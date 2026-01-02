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
import com.lakshmigarments.model.LedgerDirection;
import com.lakshmigarments.model.LorryReceipt;
import com.lakshmigarments.model.MaterialInventoryLedger;
import com.lakshmigarments.model.MovementType;
import com.lakshmigarments.model.ReferenceType;
import com.lakshmigarments.model.SubCategory;
import com.lakshmigarments.model.Supplier;
import com.lakshmigarments.model.Transport;
import com.lakshmigarments.model.User;
import com.lakshmigarments.repository.BaleRepository;
import com.lakshmigarments.repository.CategoryRepository;
import com.lakshmigarments.repository.InventoryRepository;
import com.lakshmigarments.repository.InvoiceRepository;
import com.lakshmigarments.repository.LorryReceiptRepository;
import com.lakshmigarments.repository.MaterialLedgerRepository;
import com.lakshmigarments.repository.SubCategoryRepository;
import com.lakshmigarments.repository.SupplierRepository;
import com.lakshmigarments.repository.TransportRepository;
import com.lakshmigarments.repository.UserRepository;
import com.lakshmigarments.service.impl.InventoryServiceImpl;
import jakarta.transaction.Transactional;

@Service
public class StockService {

    private final InventoryServiceImpl inventoryServiceImpl;

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
	private MaterialLedgerRepository ledgerRepository;
	private UserRepository userRepository;

	public StockService(CategoryRepository categoryRepository, TransportRepository transportRepository,
			SupplierRepository supplierRepository, SubCategoryRepository subCategoryRepository,
			ModelMapper modelMapper, InvoiceRepository invoiceRepository, BaleRepository baleRepository, 
			LorryReceiptRepository lorryReceiptRepository, InventoryRepository inventoryRepository, 
			UserRepository userRepository, InventoryServiceImpl inventoryServiceImpl, 
			MaterialLedgerRepository ledgerRepository) {
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
		this.inventoryServiceImpl = inventoryServiceImpl;
		this.ledgerRepository = ledgerRepository;
	}

	@Transactional
	public StockDTO createStock(CreateStockDTO createStockDTO) {
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
		invoice.setReceivedDate(createStockDTO.getReceivedDate());
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
				Long categoryID = baleDTO.getCategoryID();
				
				SubCategory subCategory = subCategoryRepository.findById(subCategoryID).orElseThrow(() -> {
					LOGGER.error("Sub Category with ID {} not found", subCategoryID);
					return new SubCategoryNotFoundException("Sub Category not found with ID " + subCategoryID);
				});


				Category category = categoryRepository.findById(categoryID).orElseThrow(() -> {
					LOGGER.error("Category with ID {} not found", categoryID);
					return new CategoryNotFoundException("Category not found with ID " + categoryID);
				});

				Bale bale = new Bale(baleNumber, quantity, length, price, quality, subCategory, category, lorryReceipt);
				bales.add(bale);
			}
			lrBaleMap.put(lorryReceipt, bales);
		}
		
		Invoice createdInvoice = invoiceRepository.save(invoice);
		
		for (Map.Entry<LorryReceipt, List<Bale>> entry : lrBaleMap.entrySet()) {
		    LorryReceipt lorryReceipt = entry.getKey();
		    List<Bale> bales = entry.getValue();
		    System.out.println("bale" + bales);
		    
		    lorryReceipt.setInvoice(createdInvoice);
		    LorryReceipt createdLorryReceipt = lorryReceiptRepository.save(lorryReceipt);
		    
		    for (Bale bale : bales) {
		        bale.setLorryReceipt(createdLorryReceipt);
		        baleRepository.save(bale);
		        // store in inventory
//		        Long categoryid = bale.getCategory().getId();
//		        Long subCategoryId = bale.getSubCategory().getId();
//		        
		        Inventory cacheInventory = inventoryRepository.findByCategoryIdAndSubCategoryId(bale.getCategory().getId(), bale.getSubCategory().getId()).orElse(null);
		        if (cacheInventory != null) {
		        	cacheInventory.setCount(cacheInventory.getCount() + bale.getQuantity());
				} else {
					cacheInventory = new Inventory();
					cacheInventory.setCount(bale.getQuantity());
					cacheInventory.setSubCategory(bale.getSubCategory());
					cacheInventory.setCategory(bale.getCategory());
				}
		        inventoryRepository.save(cacheInventory);
		        
		        MaterialInventoryLedger inventory;
					inventory = new MaterialInventoryLedger();
					inventory.setDirection(LedgerDirection.IN);
		        	inventory.setMovementType(MovementType.PURCHASE);
		        	inventory.setReferenceType(ReferenceType.INVOICE);
		        	inventory.setReference_id(createdInvoice.getId());
		        	inventory.setUnit("piece(s)");
					inventory.setQuantity(bale.getQuantity());
					inventory.setSubCategory(bale.getSubCategory());
					inventory.setCategory(bale.getCategory());
					inventory.setCreatedBy(user);
				
		        ledgerRepository.save(inventory);

		    }
		}

		return null;
	}
}
