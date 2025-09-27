package com.lakshmigarments.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.BaleDTO;
import com.lakshmigarments.dto.CompleteInvoiceDTO;
import com.lakshmigarments.dto.InvoiceDTO;
import com.lakshmigarments.dto.LorryReceiptDTO;
import com.lakshmigarments.exception.InvoiceNotFoundException;
import com.lakshmigarments.model.Bale;
import com.lakshmigarments.model.Invoice;
import com.lakshmigarments.model.LorryReceipt;
import com.lakshmigarments.repository.InvoiceRepository;
import com.lakshmigarments.repository.LorryReceiptRepository;
import com.lakshmigarments.repository.specification.InvoiceSpecification;

import jakarta.persistence.Tuple;

@Service
public class InvoiceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceService.class);
	private final InvoiceRepository invoiceRepository;
	private final LorryReceiptRepository lorryReceiptRepository;
	private final ModelMapper modelMapper;

	public InvoiceService(InvoiceRepository invoiceRepository, ModelMapper modelMapper,
			LorryReceiptRepository lorryReceiptRepository) {
		this.invoiceRepository = invoiceRepository;
		this.modelMapper = modelMapper;
		this.lorryReceiptRepository = lorryReceiptRepository;
	}

public Page<InvoiceDTO> getInvoices(Integer pageNo, Integer pageSize, String sortBy, String sortDir,
			String invoiceNumber, List<String> supplierNames, List<String> transportNames, List<Boolean> isPaid,
			String search, Date invoiceStartDate, Date invoiceEndDate, Date receivedStartDate, Date receivedEndDate) {

	// Set default values if needed
	if (pageNo == null) {
		pageNo = 0;
	}
	if (pageSize == null || pageSize == 0) {
		pageSize = 10;
	}

	Sort sort;
    if ("supplier".equals(sortBy)) {
        sort = sortDir.equals("asc") ? Sort.by("supplier.name").ascending() :
                                     Sort.by("supplier.name").descending();
    } else if ("transport".equals(sortBy)) {
    	sort = sortDir.equals("asc") ? Sort.by("transport.name").ascending() :
            Sort.by("transport.name").descending();
	}
    else {
        sort = sortDir.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
    }
	
	

	Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

	// Build dynamic specification based on the search parameters
	Specification<Invoice> specification = Specification
			.where(InvoiceSpecification.filterByInvoiceNumber(invoiceNumber))
			.and(InvoiceSpecification.filterBySupplierNames(supplierNames))
			.and(InvoiceSpecification.filterByTransportNames(transportNames))
			.and(InvoiceSpecification.filterByIsPaid(isPaid));

	// Apply search filter if a search string is provided
	if (search != null && !search.isEmpty()) {
		Specification<Invoice> searchSpecification = Specification.where(null);
		searchSpecification = searchSpecification.or(InvoiceSpecification.filterByInvoiceNumber(search));
		specification = specification.and(searchSpecification);
	}

	// Apply date range filters if start and end dates are provided
	if (invoiceStartDate != null && invoiceEndDate != null) {
		specification = specification.and(InvoiceSpecification.filterByInvoiceDateBetween(invoiceStartDate, invoiceEndDate));
	}
	
	if (receivedStartDate != null && receivedEndDate != null) {
		specification = specification.and(InvoiceSpecification.filterByReceivedDateBetween(receivedStartDate, receivedEndDate));
	}

	// Get the paginated result with filters
	Page<Invoice> invoicePage = invoiceRepository.findAll(specification, pageable);

	return invoicePage.map(this::convertToInvoiceDTO);
}

	public CompleteInvoiceDTO getCompleteInvoice(Long id) {

		Invoice invoice = invoiceRepository.findById(id).orElseThrow(() -> {
			LOGGER.error("Invoice with ID {} not found", id);
			return new InvoiceNotFoundException("");
		});

		CompleteInvoiceDTO completeInvoiceDTO = new CompleteInvoiceDTO();
		completeInvoiceDTO.setId(invoice.getId());
		completeInvoiceDTO.setInvoiceNumber(invoice.getInvoiceNumber());
		completeInvoiceDTO.setInvoiceDate(invoice.getInvoiceDate());
		completeInvoiceDTO.setReceivedDate(invoice.getReceivedDate());
		completeInvoiceDTO.setSupplierName(invoice.getSupplier().getName());
		completeInvoiceDTO.setTransportName(invoice.getTransport().getName());
		completeInvoiceDTO.setIsTransportPaid(invoice.getIsPaid());
		completeInvoiceDTO.setTransportCost(invoice.getTransportCost());

		completeInvoiceDTO.setNoOfLorryReceipts(invoiceRepository.findCountOfLorryReceiptsByInvoiceID(id));
		completeInvoiceDTO.setNoOfBales(invoiceRepository.findCountOfBalesByInvoiceID(id));

		Tuple totalQuantityAndValue = invoiceRepository.getTotalQuantityAndValue(id);
		System.out.println(totalQuantityAndValue.get(1).getClass().getName());
		BigDecimal quantityBigDecimal = totalQuantityAndValue.get(0, BigDecimal.class);
		
		Long quantity = quantityBigDecimal != null ? quantityBigDecimal.longValue() : null;
		Double price = totalQuantityAndValue.get(1, Double.class);
		completeInvoiceDTO.setTotalQuantity(quantity);
		completeInvoiceDTO.setValue(price);
		completeInvoiceDTO.setCategories(invoiceRepository.findDistinctCategories(id));
		completeInvoiceDTO.setSubCategories(invoiceRepository.findDistinctSubCategories(id));
		completeInvoiceDTO.setQualities(invoiceRepository.findDistinctQualities(id));
		completeInvoiceDTO.setLengths(invoiceRepository.findDistinctLengths(id));

		List<LorryReceiptDTO> lorryReceiptDTOs = new ArrayList<>();
		List<LorryReceipt> lorryReceipts = invoice.getLorryReceipts();
		for (LorryReceipt lorryReceipt : lorryReceipts) {
			List<BaleDTO> baleDTOs = new ArrayList<>();
			LorryReceiptDTO lorryReceiptDTO = modelMapper.map(lorryReceipt, LorryReceiptDTO.class);
			for (Bale bale : lorryReceipt.getBales()) {
				BaleDTO baleDTO = modelMapper.map(bale, BaleDTO.class);
				baleDTOs.add(baleDTO);
			}
			lorryReceiptDTO.setBaleDTOs(baleDTOs);
			lorryReceiptDTOs.add(lorryReceiptDTO);
		}

		completeInvoiceDTO.setLorryReceiptDTOs(lorryReceiptDTOs);

		return completeInvoiceDTO;

	}

	public InvoiceDTO updateInvoice(Long id, InvoiceDTO invoiceDTO) {
		Invoice invoice = invoiceRepository.findById(id).orElseThrow(() -> {
			LOGGER.error("Invoice with ID {} not found", id);
			return new InvoiceNotFoundException("");
		});

		invoice.setIsPaid(invoiceDTO.getIsTransportPaid());
		invoice = invoiceRepository.save(invoice);
		LOGGER.info("Invoice updated");
		return convertToInvoiceDTO(invoice);
	}

	private InvoiceDTO convertToInvoiceDTO(Invoice invoice) {
		return modelMapper.map(invoice, InvoiceDTO.class);
	}

}
