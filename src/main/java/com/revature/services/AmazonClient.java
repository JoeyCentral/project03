package com.revature.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Service
public class AmazonClient {

	private AmazonS3 s3Client;

	@Value("${amazonProperties.endpointUrl}")
	private String endpointUrl;
	@Value("${amazonProperties.bucketName}")
	private String bucketName;
	@Value("${amazonProperties.accessKey}")
	private String accessKey;
	@Value("${amazonProperties.secretKey}")
	private String secretKey;
	@Value("${amazonProperties.bucketRegion}")
	private String region;

	@PostConstruct
	@Transactional
	private void initializeAmazon() {
		BasicAWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
		s3Client = AmazonS3ClientBuilder.standard()
				.withRegion(region)
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
		System.out.println("Post Contruct:" + bucketName);
	}

	/*
	 * S3 bucket uploading method requires File as a parameter, but we have
	 * MultipartFile, so we need to add method which can make this convertion.
	 * 
	 */

	private File convertMultiPartToFile(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}

	/*
	 * This method will save a file to S3 bucket and return fileUrl which you can
	 * store to database. For example you can attach this url to user’s model if
	 * it’s a profile image etc.
	 */

	public String uploadPhoto(byte[] contents) {
		try {
			InputStream stream = new ByteArrayInputStream(contents);
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(contents.length);
			metadata.setContentType("image/png");
			String fileName = (LocalDateTime.now()).toString();
			PutObjectRequest s3Put = new PutObjectRequest(bucketName, fileName, stream, metadata);
			s3Client.putObject(s3Put);

			URL url = s3Client.getUrl(bucketName, fileName);
			return url.toString();

		} catch (AmazonServiceException e) {
			// The call was transmitted successfully, but Amazon S3 couldn't process
			// it, so it returned an error response.
			e.printStackTrace();
			return null;
		} catch (SdkClientException e) {
			// Amazon S3 couldn't be contacted for a response, or the client
			// couldn't parse the response from Amazon S3.
			e.printStackTrace();
			return null;
		}
	}

	public String deleteFileFromS3Bucket(String fileUrl) {
		String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
		s3Client.deleteObject(new DeleteObjectRequest(bucketName + "/", fileName));
		return "Successfully deleted";
	}
	
	public String getPreSignedUrl(String object) throws IOException {

        try {
            // Set the presigned URL to expire after one hour.
            java.util.Date expiration = new java.util.Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += 1000 * 60 * 60;
            expiration.setTime(expTimeMillis);

            // Generate the presigned URL.
            System.out.println("Generating pre-signed URL.");
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucketName, object)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);
            URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

            return url.toString();
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process 
            // it, so it returned an error response.
            e.printStackTrace();
            return null;
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
            return null;
        }
    }
	
}
