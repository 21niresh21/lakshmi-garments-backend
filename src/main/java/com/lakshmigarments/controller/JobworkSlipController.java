package com.lakshmigarments.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.JobworkRequestDTO;
import com.lakshmigarments.service.PdfGenerator;
import com.lakshmigarments.service.impl.JobWorkPdfGenerator;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/generate-pdf")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class JobworkSlipController {

	private final PdfGenerator pdfGenerator;

	@PostMapping(value = "/jobwork", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<byte[]> printJobWork(@RequestBody JobworkRequestDTO jobworkRequestDTO) {

		byte[] pdfBytes = pdfGenerator.generatePdf(jobworkRequestDTO);

		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=jobwork-slip.pdf").body(pdfBytes);
	}

}
