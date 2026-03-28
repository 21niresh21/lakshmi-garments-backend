package com.lakshmigarments.service.impl;

import com.lakshmigarments.dto.response.DashboardResponse;
import com.lakshmigarments.repository.InvoiceRepository;

import com.lakshmigarments.repository.MaterialLedgerRepository;
import com.lakshmigarments.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final InvoiceRepository invoiceRepository;
    
    private final MaterialLedgerRepository materialLedgerRepository;

    @Override
    public DashboardResponse getDashboardData(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 1. Weekly KPIs
        Double weeklyInvoiceCount = invoiceRepository.findTotalInvoiceCountBetweenDates(startDate, endDate);
        Double pendingPayments = invoiceRepository.findTotalPendingTransportPayments();
        Long pendingInvoiceCount = invoiceRepository.countPendingInvoices();
        Double weeklyInventoryValue = materialLedgerRepository.calculateWeeklyInventoryValue(startDateTime, endDateTime);
        Long totalQuantity = materialLedgerRepository.findTotalQuantityProcessedBetweenDates(startDateTime, endDateTime);

        List<DashboardResponse.KPI> kpis = new ArrayList<>();
         kpis.add(DashboardResponse.KPI.builder()
                 .title("Weekly Invoice Count")
                 .value(String.valueOf(weeklyInvoiceCount.intValue()))
                 .type("primary")
                 .subtitle("For selected range")
                 .build());
        kpis.add(DashboardResponse.KPI.builder()
                .title("Pending Payments")
                .value("₹" + String.format("%.2f", pendingPayments != null ? pendingPayments : 0.0))
                .type("warning")
                .subtitle("Across " + (pendingInvoiceCount != null ? pendingInvoiceCount : 0) + " invoices")
                .build());
        kpis.add(DashboardResponse.KPI.builder()
                .title("Weekly Inventory Value")
                .value("₹" + String.format("%.2f", weeklyInventoryValue != null ? weeklyInventoryValue : 0.0))
                .type("success")
                .subtitle("Value of items received")
                .build());

        kpis.add(DashboardResponse.KPI.builder()
                .title("Total Quantity (Weekly)")
                .value(String.valueOf(totalQuantity != null ? totalQuantity : 0))
                .type("secondary")
                .subtitle("Total units received")
                .build());

        // 2. Category Distribution
        List<Object[]> categoryResults = materialLedgerRepository.findCategoryDistributionBetweenDates(startDateTime, endDateTime);
        List<DashboardResponse.ChartData> categoryData = categoryResults.stream()
                .map(row -> DashboardResponse.ChartData.builder()
                        .name((String) row[0])
                        .value(((Number) row[1]).doubleValue())
                        .color(getColorForIndex(categoryResults.indexOf(row)))
                        .build())
                .collect(Collectors.toList());

        // 3. SubCategory Distribution
        List<Object[]> subCategoryResults = materialLedgerRepository.findSubCategoryDistributionBetweenDates(startDateTime, endDateTime);
        List<DashboardResponse.SubCategoryChartData> subCategoryData = subCategoryResults.stream()
                .map(row -> DashboardResponse.SubCategoryChartData.builder()
                        .name((String) row[0])
                        .value(((Number) row[1]).doubleValue())
                        .category((String) row[2])
                        .build())
                .collect(Collectors.toList());

        // 4. Supplier Performance
        List<Object[]> supplierResults = invoiceRepository.findSupplierPerformanceBetweenDates(startDate, endDate);
        List<DashboardResponse.ChartData> supplierPerformanceData = supplierResults.stream()
                .map(row -> DashboardResponse.ChartData.builder()
                        .name((String) row[0])
                        .value(((Number) row[1]).doubleValue())
                        .build())
                .collect(Collectors.toList());

        // 5. Quantity Trend
        List<Object[]> trendResults = materialLedgerRepository.findQuantityTrendBetweenDates(startDateTime, endDateTime);
        Map<LocalDate, Long> trendMap = new HashMap<>();
        for (Object[] row : trendResults) {
            LocalDate date;
            if (row[0] instanceof java.sql.Date) {
                date = ((java.sql.Date) row[0]).toLocalDate();
            } else if (row[0] instanceof LocalDate) {
                date = (LocalDate) row[0];
            } else if (row[0] instanceof java.util.Date) {
                date = ((java.util.Date) row[0]).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            } else {
                date = LocalDate.parse(row[0].toString());
            }
            trendMap.put(date, ((Number) row[1]).longValue());
        }

        List<DashboardResponse.TrendData> quantityTrendData = new ArrayList<>();
        LocalDate current = startDate;
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM");
        while (!current.isAfter(endDate)) {
            quantityTrendData.add(DashboardResponse.TrendData.builder()
                    .day(current.format(formatter))
                    .quantity(trendMap.getOrDefault(current, 0L))
                    .build());
            current = current.plusDays(1);
        }


        return DashboardResponse.builder()
                .weeklyKPIs(kpis)
                .categoryData(categoryData)
                .subCategoryData(subCategoryData)
                .supplierPerformanceData(supplierPerformanceData)
                .quantityTrendData(quantityTrendData)
                .build();
    }

    private String getColorForIndex(int index) {
        String[] colors = {"primary", "secondary", "info", "warning", "success", "error"};
        return colors[index % colors.length];
    }
}
