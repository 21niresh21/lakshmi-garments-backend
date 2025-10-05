package com.lakshmigarments.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.JobworkDetailDTO;
import com.lakshmigarments.service.JobworkService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/jobworks")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class JobworkController {

    private final JobworkService jobworkService;

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
