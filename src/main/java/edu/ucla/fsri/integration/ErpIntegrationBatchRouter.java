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

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.amqp.AMQPConnectionDetails;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints
 * to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class ErpIntegrationBatchRouter extends RouteBuilder {

	static Logger LOG = LoggerFactory.getLogger(ErpIntegrationBatchRouter.class);
	
	private Map<String, String> eventsMap;

	@Override
	public void configure() throws Exception {
		/*
		getContext().addRoutePolicyFactory(new MetricsRoutePolicyFactory());

		Namespaces ns = new Namespaces("c", "http://xmlns.oracle.com/apps/financials/commonModules/shared/model/erpIntegrationService/types/")
							.add("soap", "http://schemas.xmlsoap.org/soap/envelope/");
		
		
		//from("{{file.endpoint}}")
		from("file:input?noop=true&include=.*zip")
			.routeId("File-to-SOAP :: File Service")
			//.log("1. Base64 encode the input file... ${in.header.CamelAwsS3Key}")
			.log("1. Base64 encode the input file... ${in.headers.CamelFileName}")
			.process()
				.message(this::setValues)
			.marshal().base64()
			.convertBodyTo(String.class)
			.setHeader("author", simple("Khaleel Thotti"))
			.to("velocity:importBulkData.vm")
			.to("file:output?fileName=${in.header.CorrelationID}-100-${date:now:yyyyMMddssS}.txt&fileExist=Append")
			//.to("aws-sqs://loadAndImportData-queue?amazonSQSClient=#amazonSQSClient")
			.to("direct:loadAndImportData")
			.log("5. Done processing the zip file: ${in.header.CorrelationID}");
		
		//from("amqp:loadAndImportData")
		//from("aws-sqs://loadAndImportData-queue?amazonSQSClient=#amazonSQSClient")
		from("direct:loadAndImportData")
			.routeId("File-to-SOAP :: LoadAndImportData Service")
			.tracing("true")
			.convertBodyTo(String.class)
			.setHeader("Accept-Encoding", simple("gzip, deflate"))
			.setHeader("Authorization", simple("Basic c2Jhc2F2YTpIdXJvbjEyMyE="))
			.setHeader("SOAPAction", simple("http://xmlns.oracle.com/apps/financials/commonModules/shared/model/erpIntegrationService/importBulkData"))
			.setHeader(Exchange.HTTP_METHOD, constant("POST"))
		    .setHeader(Exchange.CONTENT_TYPE, constant("text/xml"))
		    .log("2 POSTing the SOAP Request to Oracle Cloud: importBulkData")
			.to("jetty:{{erp.endpoint.url}}")
			.process(exchange -> log.info("2.1 The response code from Oracle Cloud is:", exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE)))
			.convertBodyTo(String.class)
			.transform()
				.exchange(this::getSOAPResponse)
			//.to("file:output?fileName=${in.header.CorrelationID}-101-${date:now:yyyyMMddssS}.txt&fileExist=Append")
	        .setBody(ns.xpath("//soap:Envelope/soap:Body/c:importBulkDataResponse/c:result/text()"))
	        .convertBodyTo(String.class)
	        .log("2.2 The response from Oracle Cloud: importBulkData: ${body}")
	        .setHeader("RequestId", simple("${body}"))
	        .to("direct:audit")
	        .to("direct:essJobStatus");
			//.to("aws-sqs://essJobStatus-queue?amazonSQSClient=#amazonSQSClient");
		
		from("direct:essJobStatus")
		//from("aws-sqs://essJobStatus-queue?amazonSQSClient=#amazonSQSClient")
			.routeId("File-to-SOAP :: GetESSJobStatus Service")
			.delay(simple("60000"))
	        .to("velocity:getESSJobStatus.vm")
			.setHeader(SpringWebserviceConstants.SPRING_WS_SOAP_ACTION, simple("http://xmlns.oracle.com/apps/financials/commonModules/shared/model/erpIntegrationService/getESSJobStatus"))
		    .log("POSTing the SOAP Request to Oracle Cloud: getESSJobStatus")
			.to("spring-ws:https://ehjw-dev2.fa.us2.oraclecloud.com:443/fscmService/ErpIntegrationService?messageFactory=#messageFactory&messageSender=#messageSender&allowResponseAttachmentOverride=true")
			.process(exchange -> log.info("The response code from Oracle Cloud is:", exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE)))
			.convertBodyTo(String.class)
			.log("${body}")
		    .setBody(ns.xpath("//c:getESSJobStatusResponse/c:result/text()"))
		    .convertBodyTo(String.class)
	        .log("3.2 The response from Oracle Oracle Cloud: getESSJobStatus: ${body}")
			//.wireTap("aws-sqs://audit-queue?amazonSQSClient=#amazonSQSClient")
	        //.to("aws-sqs://essJobFinalStatus-queue?amazonSQSClient=#amazonSQSClient")
			.to("file:output?fileName=${in.header.CorrelationID}-103-${date:now:yyyyMMddssS}.txt&fileExist=Append")
			.choice()
				.when(body().isEqualTo("WAIT"))
					.to("direct:essJobStatus")
				.when(body().isEqualTo("PAUSED"))
					.to("direct:essJobStatus")
				.when(body().isEqualTo("ERROR"))
					.to("direct:downloadEssJobStatus")
				.otherwise()
					.to("direct:downloadEssJobStatus");

		
		from("direct:downloadEssJobStatus")
			.routeId("File-to-SOAP :: downloadESSJobExecutionDetails Service")
			.delay(simple("60000"))
	        .to("velocity:downlaodESSJobStatus.vm")
			.setHeader(SpringWebserviceConstants.SPRING_WS_SOAP_ACTION, simple("http://xmlns.oracle.com/apps/financials/commonModules/shared/model/erpIntegrationService/getESSJobStatus"))
		    .log("4 POSTing the SOAP Request to Oracle Cloud: downloadESSJobExecutionDetails")
			.to("spring-ws:https://ehjw-dev2.fa.us2.oraclecloud.com:443/fscmService/ErpIntegrationService?messageFactory=#messageFactory&messageSender=#messageSender&allowResponseAttachmentOverride=true")
			.transform()
				.exchange(this::downloadESSJobExecutionDetails)
			.log("${body}");



		from("file:output?noop=true&include=.*txt&delete=true")
			.routeId("Audit Service")
			.process()
			.message(this::setValues)
			.transform()
				.exchange(this::createAuditLog)
			//.log("Tap Wire route: ${headers}")
			//.to("aws-sqs://audit-queue?amazonSQSClient=#amazonSQSClient");
			//.transform()
				//.exchange(this::removeBody)
			//.setBody(constant("No Payload"))	
			//.to("amqp:audit")
			//.to("aws-sqs://audit-queue?amazonSQSClient=#amazonSQSClient");
			.log("${body}")
			 .setHeader("CamelAwsS3Key", simple("${file:name.noext}.json"))	
			 .to("aws-s3://fsri-dashboard-data?amazonS3Client=#amazonS3Client");
		
		from("file:status?noop=true&include=.*zip&delete=true")
			.setHeader("CamelAwsS3Key", simple("${file:name}"))
			.routeId("Oracle Callback Service")
			.to("{{oracle.callback}}");

	*/
	}
	
	
	private String getSOAPResponse(Exchange exchange) {
		String body = exchange.getIn().getBody(String.class);
		return body.substring(body.indexOf("<?xml"), body.indexOf(":Envelope>")+10);
		
    }

	private String downloadESSJobExecutionDetails(Exchange exchange) {
		
		Set<String> attachmentNames = exchange.getIn().getAttachmentNames();
		
		String correlationId = (String)exchange.getIn().getHeader("CorrelationID");
		
		for (String attachementName : attachmentNames) {
			DataHandler handler = exchange.getIn().getAttachment(attachementName);
			System.out.println("Content Type "+ handler.getContentType());
			try {
				File targetFile = new File("status/" + correlationId + ".zip");
				InputStream initialStream = handler.getInputStream();
				Files.copy(initialStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				IOUtils.closeQuietly(initialStream);
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
		}
		
		
		return "SUCCESS";
		
    }


	private String enrichBody(Exchange exchange) {
		String body = exchange.getIn().getBody(String.class);
		String eventId = (String)exchange.getIn().getHeader("EventID");
		String correlationID = (String)exchange.getIn().getHeader("CorrelationID");
		return correlationID + ":" + eventId + ":" + body;
		
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
		
		//add FX headers
		String camelFileLocalWorkPath = message.getHeader("CamelFileLocalWorkPath", String.class);
		if(camelFileLocalWorkPath != null && camelFileLocalWorkPath.trim().length()>0)
			message.setHeader("Localfile",camelFileLocalWorkPath);

		String camelFileHost = message.getHeader("CamelFileHost", String.class);
		if(camelFileHost != null && camelFileHost.trim().length()>0)
			message.setHeader("FTPHost",camelFileHost);
		
				
		return message;
	}
	
	private String createAuditLog(Exchange e) {
		
		String filename = e.getMessage().getHeader(Exchange.FILE_NAME, String.class);
		if(filename == null || filename.length()==0) {
			filename = e.getMessage().getHeader("CamelAwsS3Key", String.class);
			//filename = filename.substring(0, filename.indexOf('.'));
		}	
			
		
		int index = filename.indexOf('-');
		if(index !=-1) {
			filename = filename.substring(0,  index);
		}	

		try {
			Map<String, String> headers = new HashMap<String, String>();
			headers.putAll(BatchFileNameProperty.valueof(filename));
			
			if(index !=-1) {
				String camelFilename = e.getMessage().getHeader(Exchange.FILE_NAME, String.class);
				String eventId = camelFilename.substring(index+1, index+4);
				headers.put("EventID",eventId);
				headers.put("EventName", getEventName(eventId));
			}
			

			Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String auditDate = dateFormat.format(date);
            
			headers.put("auditdate", auditDate);
            
			headers.put("body", e.getIn().getBody(String.class));
			
			ObjectMapper mapperObj = new ObjectMapper();
			String jsonResp = mapperObj.writeValueAsString(headers);
			
			return jsonResp;
			
		} catch (Exception e2) {
			// TODO: handle exception
			System.out.println(e2.getMessage());
		}
		
		return "Error processing Audit";
		
    }
	
	private String getEventName(String eventId) {
		
		if(eventsMap == null) {
			eventsMap = new HashMap<>();
			eventsMap.put("100","File received from FX");
			eventsMap.put("101","File submitted to Oracle Cloud");
			eventsMap.put("102","Received Request Id from Oracle Cloud");
			eventsMap.put("103","Received ESSJob status from Oracle Cloud");
		}	
		
		return eventsMap.get(eventId);
		
	}
	
}
