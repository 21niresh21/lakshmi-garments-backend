package com.lakshmigarments.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.JobworkReceiptDTO;
import com.lakshmigarments.dto.JobworkRequestDTO;
import com.lakshmigarments.model.Jobwork;
import com.lakshmigarments.service.JobworkReceiptService;
import com.lakshmigarments.service.JobworkService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/jobwork-receipts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class JobworkReceiptController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JobworkReceiptController.class);
	
	private final JobworkReceiptService jobworkReceiptService;

	@PostMapping
    public ResponseEntity<Void> createJobworkReceipt(@RequestBody JobworkReceiptDTO jobworkReceiptDTO) {
        LOGGER.info("Received jobwork receipt request for jobwork: {}", jobworkReceiptDTO.getJobworkNumber());
        jobworkReceiptService.createJobworkReceipt(jobworkReceiptDTO);
        LOGGER.info("Jobwork created successfully for jobwork: {}", jobworkReceiptDTO.getJobworkNumber());
//        if (createdJobwork == null) {
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
	
}
