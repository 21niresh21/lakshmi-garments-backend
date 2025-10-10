package com.lakshmigarments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.JobworkDetailDTO;
import com.lakshmigarments.dto.JobworkRequestDTO;
import com.lakshmigarments.dto.JobworkResponseDTO;
import com.lakshmigarments.service.JobworkService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/jobworks")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class JobworkController {

    private final Logger LOGGER = LoggerFactory.getLogger(JobworkController.class);
    private final JobworkService jobworkService;

    @GetMapping
    public ResponseEntity<Page<JobworkResponseDTO>> getAllJobworks(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size, @RequestParam(defaultValue = "startedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order, @RequestParam(required = false) String search) {
        return new ResponseEntity<>(
                jobworkService.getAllJobworks(PageRequest.of(page, size, Sort.by(sortBy).descending()), search),
                HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Void> createJobwork(@RequestBody JobworkRequestDTO jobworkRequestDTO) {
        LOGGER.info("Received jobwork request for batch: {} and employee: {}", jobworkRequestDTO.getBatchId(),
                jobworkRequestDTO.getEmployeeId());
        jobworkService.createJobwork(jobworkRequestDTO);
        LOGGER.info("Jobwork created successfully for batch: {} and employee: {}", jobworkRequestDTO.getBatchId(),
                jobworkRequestDTO.getEmployeeId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/jobwork-numbers")
    public ResponseEntity<List<String>> getJobworkNumbers(@RequestParam(required = false) String search) {
        return new ResponseEntity<>(jobworkService.getJobworkNumbers(search), HttpStatus.OK);
    }

    @GetMapping("/jobwork-numbers/{jobworkNumber}")
    public ResponseEntity<JobworkDetailDTO> getJobworkDetail(@PathVariable String jobworkNumber) {
        return new ResponseEntity<>(jobworkService.getJobworkDetail(jobworkNumber), HttpStatus.OK);
    }

    @GetMapping("/jobwork-numbers/next-number")
    public ResponseEntity<String> getNextJobworkNumber() {
        return new ResponseEntity<>(jobworkService.getNextJobworkNumber(), HttpStatus.OK);
    }

}
