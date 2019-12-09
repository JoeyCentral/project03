package com.revature.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
    
    @GetMapping("/test")
    public String test(){
    	return "working";
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String uploadFile(HttpEntity<byte[]> requestEntity) {
    	System.out.println("received request");
    	
        return this.amazonClient.uploadPhoto(requestEntity.getBody());
    }

    @DeleteMapping("/deleteFile")
    public String deleteFile(@RequestPart(value = "url") String fileUrl) {
        return this.amazonClient.deleteFileFromS3Bucket(fileUrl);
    }
	
}
