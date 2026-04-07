package com.lakshmigarments.service;

import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.JobworkRequestDTO;

@Service
public interface PdfGenerator {
	
	public byte[] generatePdf(JobworkRequestDTO jobworkRequestDTO);

}
