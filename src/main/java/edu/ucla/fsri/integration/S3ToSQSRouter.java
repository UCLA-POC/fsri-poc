/**
 * 
 */
package edu.ucla.fsri.integration;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * @author kthotti
 *
 */
@Component
public class S3ToSQSRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {
    	/*

        from("aws-s3://camels3?amazonS3Client=#amazonS3Client")
                .routeId("ReadFromS3Route")
                .log(LoggingLevel.INFO, "Started Uploading to sqs queue")
                .to("aws-sqs://khaleel-test-queue?amazonSQSClient=#amazonSQSClient")
                .log("Completed uploading to sqs queue");
                    */

    }
}