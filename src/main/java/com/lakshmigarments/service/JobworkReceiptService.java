package com.lakshmigarments.service;

import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.request.CreateJobworkReceiptRequest;
import com.lakshmigarments.dto.request.CreateJobworkReceiptItemRequest;

@Service
public interface JobworkReceiptService {

	void createJobworkReceipt(CreateJobworkReceiptRequest jobworkReceipt);
	
}
