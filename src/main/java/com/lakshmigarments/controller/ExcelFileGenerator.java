package com.lakshmigarments.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.service.ExcelFileGeneratorService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/generate-excel")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@AllArgsConstructor
public class ExcelFileGenerator {
	
	private final ExcelFileGeneratorService fileGeneratorService;
	
	@GetMapping("/material-ledger")
	public ResponseEntity<byte[]> exportMaterialLedger() {
		
		long currentTimeMillis = System.currentTimeMillis();
		byte[] fileContent = fileGeneratorService.generateMaterialInventoryLedgers();
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
            ContentDisposition.builder("attachment").filename("Material Ledger " + currentTimeMillis + ".xlsx").build()
        );
        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
	}

}
