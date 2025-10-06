package com.lakshmigarments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.DamageTypeResponseDTO;
import com.lakshmigarments.service.DamageTypeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/damage-types")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DamageTypeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DamageTypeController.class);
    private final DamageTypeService damageTypeService;

    @GetMapping
    public ResponseEntity<List<DamageTypeResponseDTO>> getAllDamageTypes() {
        LOGGER.info("Received request to fetch all damage types");
        List<DamageTypeResponseDTO> damageTypes = damageTypeService.getAllDamageTypes();
        LOGGER.info("Returning all {} damage type(s)", damageTypes.size());
        return ResponseEntity.ok(damageTypes);
    }
}
