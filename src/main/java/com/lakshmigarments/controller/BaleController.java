package com.lakshmigarments.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.BaleDTO;
import com.lakshmigarments.service.BaleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bales")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BaleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaleController.class);
    private final BaleService baleService;

    @PutMapping("/{baleId}")
    public ResponseEntity<Void> updateBale(@PathVariable Long baleId, @RequestBody BaleDTO baleDTO) {
        LOGGER.info("Received request to update bale with id: {}", baleId);
        baleService.updateBale(baleId, baleDTO);
        LOGGER.info("Bale updated successfully with id: {}", baleId);
        return ResponseEntity.ok().build();
    }

}
