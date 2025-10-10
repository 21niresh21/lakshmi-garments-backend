package com.lakshmigarments.controller;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.CompleteInvoiceDTO;
import com.lakshmigarments.dto.InvoiceDTO;
import com.lakshmigarments.model.Supplier;
import com.lakshmigarments.service.InvoiceService;

@RestController
@RequestMapping("/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceController.class);
	private final InvoiceService invoiceService;
	
	public InvoiceController(InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}
	
	@GetMapping
    public Page<InvoiceDTO> getInvoices(@RequestParam(required = false) Integer pageNo,
                                        @RequestParam(required = false) Integer pageSize,
                                        @RequestParam(required = false, defaultValue = "invoiceDate") String sortBy,
                                        @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                        @RequestParam(required = false) String invoiceNumber,
                                        @RequestParam(required = false) List<String> supplierNames,
                                        @RequestParam(required = false) List<String> transportNames,
                                        @RequestParam(required = false) List<Boolean> isPaid,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date invoiceStartDate,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date invoiceEndDate,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date receivedStartDate,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date receivedEndDate,
                                        @RequestParam(required = false) String search) {

        return invoiceService.getInvoices(pageNo, pageSize, sortBy, sortDir, invoiceNumber, supplierNames,
        		transportNames, isPaid, search, invoiceStartDate, invoiceEndDate, receivedStartDate, receivedEndDate);
    }
	
	@GetMapping("/{id}")
	public CompleteInvoiceDTO getCompleteInvoice(@PathVariable Long id) {
		LOGGER.info("Retrieve complete invoice details for the ID {}", id);
		return invoiceService.getCompleteInvoice(id);
	}
	
	@PatchMapping("/{id}")
	public ResponseEntity<InvoiceDTO> updateInvoice(@RequestBody InvoiceDTO invoiceDTO, @PathVariable Long id) {
		LOGGER.info("Update details of invoice with ID {}", id);
		return new ResponseEntity<>(invoiceService.updateInvoice(id, invoiceDTO), HttpStatus.NO_CONTENT);
	}
	
}
