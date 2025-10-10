package com.lakshmigarments.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.ItemResponseDTO;
import com.lakshmigarments.service.BatchItemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/batch-items")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BatchItemController {

    private final Logger LOGGER = LoggerFactory.getLogger(BatchItemController.class);
    private final BatchItemService batchItemService;

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<List<ItemResponseDTO>> getItemsByBatchId(@PathVariable Long batchId) {
        LOGGER.info("Received request to get items by batch id: {}", batchId);
        List<ItemResponseDTO> items = batchItemService.getItemsByBatchId(batchId);
        LOGGER.info("Found {} items by batch id: {}", items.size(), batchId);
        return new ResponseEntity<>(items, HttpStatus.OK);
    }

}
