package com.lakshmigarments.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.PaydayDTO;
import com.lakshmigarments.dto.response.BatchItemResponse;
import com.lakshmigarments.service.PaydayService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payday")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PaydayController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PaydayController.class);
	private final PaydayService paydayService;
	
	@GetMapping("/summary")
    public ResponseEntity<Page<PaydayDTO>> getPaydaySummary(
    		@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "employeeName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate
    		) {
    	// Convert empty string to null for proper filtering
    	String employeeNameFilter = (employeeName != null && employeeName.trim().isEmpty()) ? null : employeeName;
    	
    	LOGGER.info("Fetching payday summary with filters - employeeName: {}, fromDate: {}, toDate: {}", 
    			employeeNameFilter, fromDate, toDate);
    	
		// Map sortBy to actual entity fields
		String sortByEntity;
		switch (sortBy) {
			case "employeeName":
				sortByEntity = "e.name";
				break;
			case "netWage":
			case "wage":
				sortByEntity = "netWage";
				break;
			case "grossWage":
				sortByEntity = "grossWage";
				break;
			case "completedJobworkCount":
				sortByEntity = "completedJobworkCount";
				break;
			default:
				sortByEntity = "e.name";
		}
		
		Sort sort = sortDir.equalsIgnoreCase("asc")
	            ? Sort.by(sortByEntity).ascending()
	            : Sort.by(sortByEntity).descending();
		
		Pageable pageable = PageRequest.of(page, size, sort);
        Page<PaydayDTO> paydayDTOs = paydayService.getAllPayday(employeeNameFilter, 
        		fromDate, toDate, pageable);
        
        LOGGER.info("Found {} employees with payday data", paydayDTOs.getTotalElements());
        return new ResponseEntity<>(paydayDTOs, HttpStatus.OK);
    }

}
