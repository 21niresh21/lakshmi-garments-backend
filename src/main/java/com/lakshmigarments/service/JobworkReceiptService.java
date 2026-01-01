package com.lakshmigarments.service;

import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.JobworkReceiptDTO;
import com.lakshmigarments.dto.JobworkReceiptItemDTO;

@Service
public interface JobworkReceiptService {

	void createJobworkReceipt(JobworkReceiptDTO jobworkReceipt);
	
}
