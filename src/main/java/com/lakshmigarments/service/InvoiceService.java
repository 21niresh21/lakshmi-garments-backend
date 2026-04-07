package com.lakshmigarments.service;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.lakshmigarments.context.UserContext;
import com.lakshmigarments.context.UserInfo;
import com.lakshmigarments.dto.BaleDTO;
import com.lakshmigarments.dto.CompleteInvoiceDTO;
import com.lakshmigarments.dto.InvoiceDTO;
import com.lakshmigarments.dto.LorryReceiptDTO;
import com.lakshmigarments.exception.DuplicateInvoiceException;
import com.lakshmigarments.exception.InvoiceNotFoundException;
import com.lakshmigarments.exception.SupplierNotFoundException;
import com.lakshmigarments.exception.TransportNotFoundException;
import com.lakshmigarments.model.Bale;
import com.lakshmigarments.model.Invoice;
import com.lakshmigarments.model.LorryReceipt;
import com.lakshmigarments.model.Role;
import com.lakshmigarments.model.Supplier;
import com.lakshmigarments.model.Transport;
import com.lakshmigarments.repository.InvoiceRepository;
import com.lakshmigarments.repository.LorryReceiptRepository;
import com.lakshmigarments.repository.RoleRepository;
import com.lakshmigarments.repository.SupplierRepository;
import com.lakshmigarments.repository.TransportRepository;
import com.lakshmigarments.repository.specification.InvoiceSpecification;
import com.lakshmigarments.service.policy.EditWindowPolicy;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoiceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceService.class);
	private final InvoiceRepository invoiceRepository;
	private final LorryReceiptRepository lorryReceiptRepository;
	private final ModelMapper modelMapper;
	private final SupplierRepository supplierRepository;
	private final TransportRepository transportRepository;
	private final RoleRepository roleRepository;
	private final EditWindowPolicy editWindowPolicy;


	public Page<InvoiceDTO> getInvoices(Integer pageNo, Integer pageSize, String sortBy, String sortDir,
			String invoiceNumber, List<String> supplierNames, List<String> transportNames, List<Boolean> isPaid,
			String search, LocalDate invoiceStartDate, LocalDate invoiceEndDate, LocalDate receivedStartDate, LocalDate receivedEndDate) {

		// Set default values if needed
		if (pageNo == null) {
			pageNo = 0;
		}
		if (pageSize == null || pageSize == 0) {
			pageSize = 10;
		}

		Sort sort;
		if ("supplier".equals(sortBy)) {
			sort = sortDir.equals("asc") ? Sort.by("supplier.name").ascending() : Sort.by("supplier.name").descending();
		} else if ("transport".equals(sortBy)) {
			sort = sortDir.equals("asc") ? Sort.by("transport.name").ascending()
					: Sort.by("transport.name").descending();
		} else {
			sort = sortDir.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		}

		Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
		
		System.out.println("from service + " + invoiceStartDate + " " + invoiceEndDate);
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
			searchSpecification = searchSpecification.or(InvoiceSpecification.filterByBaleNumber(search));
			specification = specification.and(searchSpecification);
		}

		// Apply date range filters if at least one date is provided
		if (invoiceStartDate != null || invoiceEndDate != null) {
			specification = specification
					.and(InvoiceSpecification.filterByInvoiceDateBetween(invoiceStartDate, invoiceEndDate));
		}

		if (receivedStartDate != null || receivedEndDate != null) {
			specification = specification
					.and(InvoiceSpecification.filterByReceivedDateBetween(receivedStartDate, receivedEndDate));
		}

		// Get the paginated result with filters
		Page<Invoice> invoicePage = invoiceRepository.findAll(specification, pageable);

		return invoicePage.map(this::convertToInvoiceDTO);
	}

	public CompleteInvoiceDTO getCompleteInvoice(Long id) {
		
		UserInfo user = UserContext.get();

		Invoice invoice = invoiceRepository.findById(id).orElseThrow(() -> {
			LOGGER.error("Invoice with ID {} not found", id);
			return new InvoiceNotFoundException("");
		});
		
		Authentication auth =
			    SecurityContextHolder.getContext().getAuthentication();
		String role = auth.getAuthorities().stream()
		        .map(grantedAuthority -> grantedAuthority.getAuthority())
		        .findFirst()
		        .map(r -> r.replace("ROLE_", ""))
		        .orElse(null);
		

		boolean canEdit = editWindowPolicy.canEdit(invoice.getCreatedAt(), role);

		CompleteInvoiceDTO completeInvoiceDTO = new CompleteInvoiceDTO();
		completeInvoiceDTO.setId(invoice.getId());
		completeInvoiceDTO.setInvoiceNumber(invoice.getInvoiceNumber());
		completeInvoiceDTO.setInvoiceDate(invoice.getInvoiceDate());
		completeInvoiceDTO.setReceivedDate(invoice.getReceivedDate());
		completeInvoiceDTO.setSupplierName(invoice.getSupplier().getName());
		completeInvoiceDTO.setTransportName(invoice.getTransport().getName());
		completeInvoiceDTO.setIsTransportPaid(invoice.getIsPaid());
		completeInvoiceDTO.setTransportCost(invoice.getTransportCost());
		completeInvoiceDTO.setCanEdit(canEdit);

		completeInvoiceDTO.setNoOfLorryReceipts(invoiceRepository.findCountOfLorryReceiptsByInvoiceID(id));
		completeInvoiceDTO.setNoOfBales(invoiceRepository.findCountOfBalesByInvoiceID(id));

		Tuple totalQuantityAndValue = invoiceRepository.getTotalQuantityAndValue(id);
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
				System.out.println(bale.getCategory());
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
		
		boolean isDuplicate = invoiceRepository.existsByInvoiceNumberAndSupplierNameAndIdNot(
				invoiceDTO.getInvoiceNumber(), invoiceDTO.getSupplierName(), id);
		if (isDuplicate) {
			LOGGER.error("Duplicate Invoice");
			throw new DuplicateInvoiceException("Invoice already exists with same "
					+ "invoice number and supplier");
		}

		if (invoiceDTO.getInvoiceNumber() != null) {
			invoice.setInvoiceNumber(invoiceDTO.getInvoiceNumber());
		}
		if (invoiceDTO.getInvoiceDate() != null) {
			invoice.setInvoiceDate(invoiceDTO.getInvoiceDate());
		}
		if (invoiceDTO.getReceivedDate() != null) {
			invoice.setReceivedDate(invoiceDTO.getReceivedDate());
		}
		if (invoiceDTO.getSupplierName() != null) {
			Supplier supplier = supplierRepository.findByNameIgnoreCase(invoiceDTO.getSupplierName())
					.orElseThrow(() -> {
						LOGGER.error("Supplier with name {} not found", invoiceDTO.getSupplierName());
						return new SupplierNotFoundException(
								"Supplier not found with name " + invoiceDTO.getSupplierName());
					});
			invoice.setSupplier(supplier);
		}
		if (invoiceDTO.getTransportName() != null) {
			Transport transport = transportRepository.findByNameIgnoreCase(invoiceDTO.getTransportName())
					.orElseThrow(() -> {
						LOGGER.error("Transport with name {} not found", invoiceDTO.getTransportName());
						return new TransportNotFoundException(
								"Transport not found with name " + invoiceDTO.getTransportName());
					});
			invoice.setTransport(transport);
		}
		if (invoiceDTO.getTransportCost() != null) {
			invoice.setTransportCost(invoiceDTO.getTransportCost());
		}
		if (invoiceDTO.getIsTransportPaid() != null) {
			invoice.setIsPaid(invoiceDTO.getIsTransportPaid());
		}
		invoice = invoiceRepository.save(invoice);
		LOGGER.info("Invoice updated successfully with ID: {}", invoice.getId());
		return convertToInvoiceDTO(invoice);
	}

	private InvoiceDTO convertToInvoiceDTO(Invoice invoice) {
		return modelMapper.map(invoice, InvoiceDTO.class);
	}

}
