package com.lakshmigarments.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.LorryReceiptUpdateDTO;
import com.lakshmigarments.service.LorryReceiptService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/lorry-receipts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LorryReceiptController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LorryReceiptController.class);
    private final LorryReceiptService lorryReceiptService;

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateLorryReceipt(@PathVariable Long id,
            @RequestBody LorryReceiptUpdateDTO lorryReceiptUpdateDTO) {
        LOGGER.info("Received request to update lorry receipt with id: {}", id);
        lorryReceiptService.updateLorryReceipt(id, lorryReceiptUpdateDTO);
        LOGGER.info("Lorry receipt updated successfully with id: {}", id);
        return ResponseEntity.ok().build();
    }
}
