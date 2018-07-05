package edu.ucla.fsri.integration;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class HelloCamelRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {
    	/*
        from("timer:hello?period={{timer.period}}")
        	.routeId("HelloService")
            .setBody().constant("Hello Camel!")
            .to("stream:out")
            .to("mock:result");
            //.to("aws-sqs://hello-world-queue?amazonSQSClient=#amazonSQSClient")
            //.log("Completed uploading to sqs queue");
             */

    
    }

}
