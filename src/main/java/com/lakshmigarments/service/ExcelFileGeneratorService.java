package com.lakshmigarments.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lakshmigarments.model.MaterialInventoryLedger;

@Service
public interface ExcelFileGeneratorService {
	
	byte[] generateMaterialInventoryLedgers();

}
