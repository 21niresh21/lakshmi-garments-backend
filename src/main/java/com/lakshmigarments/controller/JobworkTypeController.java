package com.lakshmigarments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.JobworkTypeResponseDTO;
import com.lakshmigarments.service.JobworkTypeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/jobwork-types")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class JobworkTypeController {
    private final Logger LOGGER = LoggerFactory.getLogger(JobworkTypeController.class);
    private final JobworkTypeService jobworkTypeService;

    @GetMapping
    public ResponseEntity<List<JobworkTypeResponseDTO>> getAllJobworkTypes() {
        LOGGER.info("Received request to fetch all jobwork types");
        List<JobworkTypeResponseDTO> jobworkTypes = jobworkTypeService.getAllJobworkTypes();
        LOGGER.info("Returning all {} jobwork type(s)", jobworkTypes.size());
        return ResponseEntity.ok(jobworkTypes);
    }
}
