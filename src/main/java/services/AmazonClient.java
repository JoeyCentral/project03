package services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
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

	public String uploadFile(MultipartFile multipartFile) {

		String fileUrl = "";
		try {
			File file = convertMultiPartToFile(multipartFile);
			String fileName = generateFileName(multipartFile);
			fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
			uploadFileTos3bucket(fileName, file);
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileUrl;
	}

	/*
	 * Also you can upload the same file many times, so we should generate unique
	 * name for each of them. Let’s use a timestamp and also replace all spaces in
	 * filename with underscores to avoid issues in future.
	 * 
	 */
	private String generateFileName(MultipartFile multiPart) {
		return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
	}

	/*
	 * method which uploads file to S3 bucket.
	 */
	private void uploadFileTos3bucket(String fileName, File file) {
		s3Client.putObject(new PutObjectRequest(bucketName, fileName, file));
	}

	public String deleteFileFromS3Bucket(String fileUrl) {
		String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
		s3Client.deleteObject(new DeleteObjectRequest(bucketName + "/", fileName));
		return "Successfully deleted";
	}
}
