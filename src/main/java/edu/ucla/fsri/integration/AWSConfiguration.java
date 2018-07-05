/**
 * 
 */
package edu.ucla.fsri.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

/**
 * @author kthotti
 *
 */
@Configuration
public class AWSConfiguration {

		
	   //private AWSCredentials awsCredentials = new BasicAWSCredentials(System.getProperty("accessKey"), System.getProperty("secretKey"));

	    @Bean public AmazonS3 amazonS3Client() {
	    	AWSCredentials awsCredentials = new BasicAWSCredentials("AKIAILP6FFCHCOQQTWDQ", "TLQKbsdHBHiHWIVaxSeC8ULUP4mldXlHyVJz3ptN");
            AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
            AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder.standard().withRegion(Regions.US_WEST_2).withCredentials(credentialsProvider);
            return clientBuilder.build();
	    }

	    @Bean public AmazonSQS amazonSQSClient() {
	    	AWSCredentials awsCredentials = new BasicAWSCredentials("AKIAILP6FFCHCOQQTWDQ", "TLQKbsdHBHiHWIVaxSeC8ULUP4mldXlHyVJz3ptN");
            AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
            AmazonSQSClientBuilder clientBuilder = AmazonSQSClientBuilder.standard().withRegion(Regions.US_WEST_2).withCredentials(credentialsProvider);
            return clientBuilder.build();
	    }
}