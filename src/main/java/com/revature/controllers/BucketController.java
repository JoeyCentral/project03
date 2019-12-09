package com.revature.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.revature.services.AmazonClient;

@RestController
public class BucketController {
	
	AmazonClient amazonClient;

    @Autowired
    BucketController(AmazonClient amazonClient) {
    	super();
        this.amazonClient = amazonClient;
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public String uploadFile(HttpEntity<byte[]> requestEntity) {
        return this.amazonClient.uploadPhoto(requestEntity.getBody());
    }

    @DeleteMapping("/deleteFile")
    public String deleteFile(@RequestPart(value = "url") String fileUrl) {
        return this.amazonClient.deleteFileFromS3Bucket(fileUrl);
    }
    
    @PostMapping("/url/{object}")
    public String getPreSignedUrl(@PathVariable String object) throws IOException {
    	System.out.println("presign request received");
    	return amazonClient.getPreSignedUrl(object);
    }
	
}
