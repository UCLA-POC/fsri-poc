/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.ucla.fsri.integration;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.component.amqp.AMQPConnectionDetails;
import org.apache.camel.component.metrics.routepolicy.MetricsRoutePolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints
 * to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class GLInterfaceRouter extends RouteBuilder {

	static Logger LOG = LoggerFactory.getLogger(GLInterfaceRouter.class);
	
    //public static String AMQP_URL = "amqps://b-cdddd1ff-58bc-4c68-98ab-6b558e2fcdc5-1.mq.us-west-2.amazonaws.com:5671";
    //public static String BROKER_USERNAME = "fsri";
    //public static String BROKER_PASSWORD = "f$R1d3m02018";

	
	@Value("${spring.activemq.broker-url}")
	private String AMQP_URL;

	@Value("${spring.activemq.user}")
	private String BROKER_USERNAME;
    
	@Value("${spring.activemq.password}")
	private String BROKER_PASSWORD;


	@Override
	public void configure() throws Exception {
		
		getContext().addRoutePolicyFactory(new MetricsRoutePolicyFactory());

		Namespaces ns = new Namespaces("c", "http://xmlns.oracle.com/apps/financials/commonModules/shared/model/erpIntegrationService/types/")
							.add("soap", "http://schemas.xmlsoap.org/soap/envelope/");
		
		//from("file:input?noop=true&include=.*zip")
		from("aws-s3://fsri-poc?amazonS3Client=#amazonS3Client&deleteAfterRead=true")
			.routeId("File-to-SOAP :: Base64 Service")
			.log("1. Base64 encode the input file... ${in.header.CamelAwsS3Key}")
			//.setHeader("CamelFileName", constant("${in.header.CamelAwsS3Key}"))
			.process()
				.message(this::setValues)
			.marshal().base64()
			.convertBodyTo(String.class)
			.setHeader("author", simple("Khaleel Thotti"))
			.to("velocity:importBulkData.vm")
			//.to("file:output?fileName=${in.header.CorrelationID}-import-bulk-data-request-${date:now:yyyyMMddssS}.txt&fileExist=Append")
			.to("file:output?fileName=${in.header.CorrelationID}-100-${date:now:yyyyMMddssS}.txt&fileExist=Append")
			
			//.wireTap("aws-sqs://audit-queue?amazonSQSClient=#amazonSQSClient", true, constant("Bye World"))
			
			/*
			.wireTap("amqp:audit")
			.process(exchange -> {
                exchange.setProperty("fileName", "${in.header.CorrelationID}");
            })
            */
			
			//.to("aws-sqs://loadAndImportData-queue?amazonSQSClient=#amazonSQSClient")
			.to("direct:loadAndImportData")
			.log("5. Done processing the zip file: ${in.header.CorrelationID}");
		
//		from("amqp:loadAndImportData")
		//from("aws-sqs://loadAndImportData-queue?amazonSQSClient=#amazonSQSClient")
		from("direct:loadAndImportData")
			.routeId("File-to-SOAP :: LoadAndImportData Service")
			.tracing("true")
			.convertBodyTo(String.class)
			.setHeader("Authorization", simple("Basic c2Jhc2F2YTpIdXJvbjEyMyE="))
			.setHeader("Accept-Encoding", simple("gzip, deflate"))
			.setHeader("SOAPAction", simple("http://xmlns.oracle.com/apps/financials/commonModules/shared/model/erpIntegrationService/importBulkData"))
			.setHeader(Exchange.HTTP_METHOD, constant("POST"))
		    .setHeader(Exchange.CONTENT_TYPE, constant("text/xml"))
		    .log("2 POSTing the SOAP Request to Oracle Cloud: importBulkData")
			.to("restlet:{{gl.interface.endpoint.url}}")
			//.process(exchange -> log.info("4.1 The response code from Oracle Financials Cloud ErpIntegrationService is: {}", exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE)))
			.convertBodyTo(String.class)
			.transform()
				.exchange(this::getSOAPResponse)
			.to("file:output?fileName=${in.header.CorrelationID}-101-${date:now:yyyyMMddssS}.txt&fileExist=Append")
	        .setBody(ns.xpath("//soap:Envelope/soap:Body/c:importBulkDataResponse/c:result/text()"))
	        .convertBodyTo(String.class)
	        .log("2.1 The response from Oracle Cloud: importBulkData: ${body}") 
			.to("direct:essJobStatus");
			//.to("aws-sqs://essJobStatus-queue?amazonSQSClient=#amazonSQSClient");
		
		from("direct:essJobStatus")
		//from("aws-sqs://essJobStatus-queue?amazonSQSClient=#amazonSQSClient")
			.routeId("File-to-SOAP :: GetESSJobStatus Service")
	        .to("velocity:getESSJobStatus.vm")
			//.wireTap("aws-sqs://audit-queue?amazonSQSClient=#amazonSQSClient")
	        //.to("file:output?fileName=essjobstatus-request-${in.header.CorrelationID}-${date:now:yyyyMMddssS}.txt")
			.setHeader("Authorization", simple("Basic c2Jhc2F2YTpIdXJvbjEyMyE="))
			.setHeader("Accept-Encoding", simple("gzip, deflate"))
			.setHeader("SOAPAction", simple("http://xmlns.oracle.com/apps/financials/commonModules/shared/model/erpIntegrationService/getESSJobStatus"))
			.setHeader(Exchange.HTTP_METHOD, constant("POST"))
		    .setHeader(Exchange.CONTENT_TYPE, constant("text/xml"))
		    .log("3 POSTing the SOAP Request to Oracle Cloud: getESSJobStatus")
			.to("restlet:{{gl.interface.endpoint.url}}")
			//.process(exchange -> log.info("4.2 The response code from Oracle Financials Cloud ErpIntegrationService is: {}", exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE)))
			.convertBodyTo(String.class)
			.transform()
				.exchange(this::getSOAPResponse)
			.to("file:output?fileName=${in.header.CorrelationID}-102-${date:now:yyyyMMddssS}.txt&fileExist=Append")
			//.wireTap("aws-sqs://audit-queue?amazonSQSClient=#amazonSQSClient")
	        .setBody(ns.xpath("//soap:Envelope/soap:Body/c:getESSJobStatusResponse/c:result/text()"))
	        .convertBodyTo(String.class)
	        .log("3.1 The response from Oracle Oracle Cloud: getESSJobStatus: ${body}")
			//.wireTap("aws-sqs://audit-queue?amazonSQSClient=#amazonSQSClient")
	        //.to("aws-sqs://essJobFinalStatus-queue?amazonSQSClient=#amazonSQSClient")
			.to("file:output?fileName=${in.header.CorrelationID}-103-${date:now:yyyyMMddssS}.txt&fileExist=Append");
			/*
			.wireTap("amqp:audit")
			.process(exchange -> {
                exchange.setProperty("fileName", "${file:name.noext}");
            });
            */
		
		
			from("file:output?noop=true&include=.*txt&delete=false")
				.process()
				.message(this::setValues)
				.log("Tap Wire route: ${headers}")
				//.to("aws-sqs://audit-queue?amazonSQSClient=#amazonSQSClient");
				.transform()
					.exchange(this::removeBody)
				//.setBody(constant("No Payload"))	
				//.to("amqp:audit")
				.to("aws-sqs://audit-queue?amazonSQSClient=#amazonSQSClient");
				//.log("${body}");	
	}
	
	private String getSOAPResponse(Exchange exchange) {
		String body = exchange.getIn().getBody(String.class);
		return body.substring(body.indexOf("<?xml"), body.indexOf(":Envelope>")+10);
		
    }

	private String removeBody(Exchange exchange) {
		String body = exchange.getIn().getBody(String.class);
		String eventId = (String)exchange.getIn().getHeader("EventID");
		return eventId + ":" + body;
		
    }

    @Bean
    AMQPConnectionDetails securedAmqpConnection() {
        AMQPConnectionDetails amqpConnectionDetails = new AMQPConnectionDetails(AMQP_URL, BROKER_USERNAME, BROKER_PASSWORD);
        return amqpConnectionDetails;
    }

    
	private Message setValues(Message message) {		
		
		String filename = message.getHeader(Exchange.FILE_NAME, String.class);
		if(filename == null || filename.length()==0) {
			filename = message.getHeader("CamelAwsS3Key", String.class);
			//filename = filename.substring(0, filename.indexOf('.'));
		}	
			
		
		int index = filename.indexOf('-');
		if(index !=-1) {
			filename = filename.substring(0,  index);
		}	
		message.getHeaders().putAll(BatchFileNameProperty.valueof(filename));
		if(index !=-1) {
			String camelFilename = message.getHeader(Exchange.FILE_NAME, String.class);
			String eventId = camelFilename.substring(index+1, index+4);
			message.setHeader("EventID",eventId);
		}	

		return message;
	}


}
