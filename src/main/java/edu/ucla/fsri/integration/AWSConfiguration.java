/**
 * 
 */
package edu.ucla.fsri.integration;

import org.springframework.beans.factory.annotation.Value;
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

	@Value("${aws.access.key}")
	private String AWS_ACCESS_KEY;

	@Value("${aws.secret.key}")
	private String AWS_SECRET_KEY;


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
}