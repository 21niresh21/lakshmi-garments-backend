package com.lakshmigarments.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.CreateBaleDTO;
import com.lakshmigarments.dto.CreateLorryReceiptDTO;
import com.lakshmigarments.dto.CreateStockDTO;
import com.lakshmigarments.dto.StockDTO;
import com.lakshmigarments.exception.CategoryNotFoundException;
import com.lakshmigarments.exception.SubCategoryNotFoundException;
import com.lakshmigarments.exception.SupplierNotFoundException;
import com.lakshmigarments.exception.TransportNotFoundException;
import com.lakshmigarments.exception.UserNotFoundException;
import com.lakshmigarments.model.Bale;
import com.lakshmigarments.model.Category;
import com.lakshmigarments.model.Inventory;
import com.lakshmigarments.model.Invoice;
import com.lakshmigarments.model.LorryReceipt;
import com.lakshmigarments.model.SubCategory;
import com.lakshmigarments.model.Supplier;
import com.lakshmigarments.model.Transport;
import com.lakshmigarments.model.User;
import com.lakshmigarments.repository.BaleRepository;
import com.lakshmigarments.repository.CategoryRepository;
import com.lakshmigarments.repository.InventoryRepository;
import com.lakshmigarments.repository.InvoiceRepository;
import com.lakshmigarments.repository.LorryReceiptRepository;
import com.lakshmigarments.repository.SubCategoryRepository;
import com.lakshmigarments.repository.SupplierRepository;
import com.lakshmigarments.repository.TransportRepository;
import com.lakshmigarments.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class StockService {

	private static final Logger LOGGER = LoggerFactory.getLogger(StockService.class);
	private final CategoryRepository categoryRepository;
	private final TransportRepository transportRepository;
	private final SupplierRepository supplierRepository;
	private final SubCategoryRepository subCategoryRepository;
	private final InvoiceRepository invoiceRepository;
	private final ModelMapper modelMapper;
	private BaleRepository baleRepository;
	private LorryReceiptRepository lorryReceiptRepository;
	private InventoryRepository inventoryRepository;
	private UserRepository userRepository;

	public StockService(CategoryRepository categoryRepository, TransportRepository transportRepository,
			SupplierRepository supplierRepository, SubCategoryRepository subCategoryRepository,
			ModelMapper modelMapper, InvoiceRepository invoiceRepository, BaleRepository baleRepository, 
			LorryReceiptRepository lorryReceiptRepository, InventoryRepository inventoryRepository, UserRepository userRepository) {
		this.categoryRepository = categoryRepository;
		this.transportRepository = transportRepository;
		this.supplierRepository = supplierRepository;
		this.subCategoryRepository = subCategoryRepository;
		this.modelMapper = modelMapper;
		this.invoiceRepository = invoiceRepository;
		this.baleRepository = baleRepository;
		this.lorryReceiptRepository = lorryReceiptRepository;
		this.inventoryRepository = inventoryRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public StockDTO createStock(CreateStockDTO createStockDTO) {
		System.out.println(createStockDTO);
		Long supplierID = createStockDTO.getSupplierID();
		Long transportID = createStockDTO.getTransportID();
		List<CreateLorryReceiptDTO> lorryReceiptDTOs = createStockDTO.getLorryReceipts();

		Supplier supplier = supplierRepository.findById(supplierID).orElseThrow(() -> {
			LOGGER.error("Supplier with ID {} not found", supplierID);
			return new SupplierNotFoundException("Supplier not found with ID " + supplierID);
		});
		
		Transport transport = transportRepository.findById(transportID).orElseThrow(() -> {
			LOGGER.error("Transport with ID {} not found", transportID);
			return new TransportNotFoundException("Transport not found with ID " + transportID);
		});
		
		User user = userRepository.findById(createStockDTO.getCreatedById()).orElseThrow(() -> {
			LOGGER.error("User with ID {} not found", createStockDTO.getCreatedById());
			return new UserNotFoundException("User not found with ID " + createStockDTO.getCreatedById());
		});
		
		Invoice invoice = new Invoice();
		invoice.setInvoiceDate(createStockDTO.getInvoiceDate());
		invoice.setInvoiceNumber(createStockDTO.getInvoiceNumber());
		invoice.setReceivedDate(createStockDTO.getShipmentReceivedDate());
		if (createStockDTO.getTransportCost() == null) {
			invoice.setTransportCost(0d);
		} else {
			invoice.setTransportCost(createStockDTO.getTransportCost());
		}
		
		invoice.setIsPaid(createStockDTO.getIsTransportPaid());
		invoice.setTransport(transport);
		invoice.setSupplier(supplier);
		invoice.setCreatedBy(user);
		
		HashMap<LorryReceipt, List<Bale>> lrBaleMap = new HashMap<>();
		
		for (CreateLorryReceiptDTO createLorryReceiptDTO : lorryReceiptDTOs) {
			String LRNumber = createLorryReceiptDTO.getLrNumber();
			List<CreateBaleDTO> baleDTOs = createLorryReceiptDTO.getBales();
			List<Bale> bales = new ArrayList<>();
			
			LorryReceipt lorryReceipt = new LorryReceipt();
			lorryReceipt.setLRNumber(LRNumber);
			
			for (CreateBaleDTO baleDTO : baleDTOs) {
				String baleNumber = baleDTO.getBaleNumber();
				Long quantity = baleDTO.getQuantity();
				Double length = baleDTO.getLength();
				Double price = baleDTO.getPrice();
				String quality = baleDTO.getQuality();
				Long subCategoryID = baleDTO.getSubCategoryID();

				SubCategory subCategory = subCategoryRepository.findById(subCategoryID).orElseThrow(() -> {
					LOGGER.error("Sub Category with ID {} not found", subCategoryID);
					return new SubCategoryNotFoundException("Sub Category not found with ID " + subCategoryID);
				});
				
				Bale bale = new Bale(baleNumber, quantity, length, price, quality, subCategory, lorryReceipt);
				bales.add(bale);
			}
			lrBaleMap.put(lorryReceipt, bales);
		}
		
		Invoice createdInvoice = invoiceRepository.save(invoice);
		
		for (Map.Entry<LorryReceipt, List<Bale>> entry : lrBaleMap.entrySet()) {
		    LorryReceipt lorryReceipt = entry.getKey();
		    List<Bale> bales = entry.getValue();
		    
		    lorryReceipt.setInvoice(createdInvoice);
		    LorryReceipt createdLorryReceipt = lorryReceiptRepository.save(lorryReceipt);
		    
		    for (Bale bale : bales) {
		        bale.setLorryReceipt(createdLorryReceipt);
		        baleRepository.save(bale);
		        // store in inventory
		        Inventory inventory = inventoryRepository.findBySubCategoryName(bale.getSubCategory().getName()).orElse(null);
		        if (inventory != null) {
					inventory.setCount(inventory.getCount() + bale.getQuantity());
				} else {
					inventory = new Inventory();
					inventory.setCount(bale.getQuantity());
					inventory.setSubCategory(bale.getSubCategory());
				}
		        inventoryRepository.save(inventory);

		    }
		}

		return null;
	}
}
