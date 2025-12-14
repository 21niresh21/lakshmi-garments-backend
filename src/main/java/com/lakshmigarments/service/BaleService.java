package com.lakshmigarments.service;

import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.BaleDTO;

@Service
public interface BaleService {
	
	public void updateBale(Long baleId, BaleDTO baleDTO);

}
