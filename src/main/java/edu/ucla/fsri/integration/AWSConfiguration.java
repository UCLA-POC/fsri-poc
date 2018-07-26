/**
 * 
 */
package edu.ucla.fsri.integration;

import org.apache.camel.component.amqp.AMQPConnectionDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author kthotti
 *
 */
@Configuration
@ImportResource({"classpath*:applicationContext.xml"})
@EnableSwagger2
public class AWSConfiguration {

	@Value("${aws.access.key}")
	private String AWS_ACCESS_KEY;

	@Value("${aws.secret.key}")
	private String AWS_SECRET_KEY;
	
	/*
	@Value("${spring.activemq.broker-url}")
	private String AMQP_URL;

	@Value("${spring.activemq.user}")
	private String BROKER_USERNAME;
    
	@Value("${spring.activemq.password}")
	private String BROKER_PASSWORD;
	*/



	@Bean
	public AmazonS3 amazonS3Client() {
		AWSCredentials awsCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY);
		AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
		AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder.standard().withRegion(Regions.US_WEST_2).withCredentials(credentialsProvider);
		return clientBuilder.build();
	}

	@Bean
	public AmazonSQS amazonSQSClient() {
		AWSCredentials awsCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY);
		AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
		AmazonSQSClientBuilder clientBuilder = AmazonSQSClientBuilder.standard().withRegion(Regions.US_WEST_2).withCredentials(credentialsProvider);
		return clientBuilder.build();
	}

	@Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("edu.ucla.fsri.integration"))
                .paths(PathSelectors.any())
                .build();
    }
	
	/*
	@Bean
    AMQPConnectionDetails securedAmqpConnection() {
        AMQPConnectionDetails amqpConnectionDetails = new AMQPConnectionDetails(AMQP_URL, BROKER_USERNAME, BROKER_PASSWORD);
        return amqpConnectionDetails;
    }
    */


}