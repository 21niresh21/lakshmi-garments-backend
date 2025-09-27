package com.lakshmigarments.configuration;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.lakshmigarments.dto.BaleDTO;
import com.lakshmigarments.dto.BatchResponseDTO;
import com.lakshmigarments.dto.BatchResponseDTO.BatchSubCategoryResponseDTO;
import com.lakshmigarments.dto.CreateSupplierDTO;
import com.lakshmigarments.dto.InvoiceDTO;
import com.lakshmigarments.dto.LorryReceiptDTO;
import com.lakshmigarments.model.Bale;
import com.lakshmigarments.model.Batch;
import com.lakshmigarments.model.BatchSubCategory;
import com.lakshmigarments.model.Invoice;
import com.lakshmigarments.model.LorryReceipt;

@Configuration
public class ModelMapperConfig {

	@Bean
	public ModelMapper modelMapper() {
		
		ModelMapper modelMapper = new ModelMapper();
		
		modelMapper.typeMap(Invoice.class, InvoiceDTO.class).addMappings(mapper -> {
        	mapper.map(src -> src.getSupplier().getName(), InvoiceDTO::setSupplierName);
        	mapper.map(src -> src.getTransport().getName(), InvoiceDTO::setTransportName);
        	mapper.map(src -> src.getIsPaid(), InvoiceDTO::setIsTransportPaid);
        });
		
		modelMapper.typeMap(Bale.class, BaleDTO.class).addMappings(mapper -> {
        	mapper.map(src -> src.getSubCategory().getCategory().getName(), BaleDTO::setCategory);
        	mapper.map(src -> src.getSubCategory().getName(), BaleDTO::setSubCategory);
        });
		
		modelMapper.typeMap(Batch.class, BatchResponseDTO.class).addMappings(mapper -> {
			mapper.map(src -> src.getCategory().getName(), BatchResponseDTO::setCategoryName);
			mapper.map(src -> src.getBatchStatus().getName(), BatchResponseDTO::setBatchStatus);
		});
		
		modelMapper.typeMap(BatchSubCategory.class, BatchSubCategoryResponseDTO.class).addMappings(mapper -> {
			mapper.map(src -> src.getSubCategory().getName(), BatchSubCategoryResponseDTO::setSubCategoryName);
		});
		
		return modelMapper;
				
	}
}
