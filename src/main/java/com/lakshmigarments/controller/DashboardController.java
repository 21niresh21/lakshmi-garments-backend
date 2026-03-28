package com.lakshmigarments.controller;

import com.lakshmigarments.dto.response.DashboardResponse;
import com.lakshmigarments.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('Super Admin', 'Accounts Admin')")
    public ResponseEntity<DashboardResponse> getDashboardData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null || endDate == null) {
            // Calculate the most recent Sunday as start date
            LocalDate today = LocalDate.now();
            startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
            // Set end date to the following Saturday
            endDate = startDate.plusDays(6);
        }

        DashboardResponse response = dashboardService.getDashboardData(startDate, endDate);
        return ResponseEntity.ok(response);
    }
}
