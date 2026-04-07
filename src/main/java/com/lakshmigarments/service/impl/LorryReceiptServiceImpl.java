package com.lakshmigarments.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.lakshmigarments.dto.LorryReceiptUpdateDTO;
import com.lakshmigarments.exception.LorryReceiptNotFoundException;
import com.lakshmigarments.model.LorryReceipt;
import com.lakshmigarments.repository.LorryReceiptRepository;
import com.lakshmigarments.service.LorryReceiptService;

@Service
@RequiredArgsConstructor
public class LorryReceiptServiceImpl implements LorryReceiptService {

	private final Logger LOGGER = LoggerFactory.getLogger(LorryReceiptServiceImpl.class);
	private final LorryReceiptRepository lorryReceiptRepository;

	@Override
	public void updateLorryReceipt(Long id, LorryReceiptUpdateDTO lorryReceiptUpdateDTO) {
		LorryReceipt lorryReceipt = lorryReceiptRepository.findById(id).orElseThrow(() -> {
			LOGGER.error("Lorry Receipt not found with id: {}", id);
			return new LorryReceiptNotFoundException("Lorry Receipt not found with id: " + id);
		});

		if (lorryReceiptUpdateDTO.getLrNumber() != null) {
			lorryReceipt.setLrNumber(lorryReceiptUpdateDTO.getLrNumber());
		}
		lorryReceiptRepository.save(lorryReceipt);
	}
}
