package com.lakshmigarments.service;

import com.lakshmigarments.dto.CreateStockDTO;
import com.lakshmigarments.dto.StockDTO;

public interface StockService {
	
	StockDTO createStock(CreateStockDTO createStockDTO);

}
