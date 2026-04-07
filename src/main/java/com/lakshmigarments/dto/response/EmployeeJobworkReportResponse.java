package com.lakshmigarments.dto.response;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Top-level DTO for the employee jobwork detailed report, including overall statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeJobworkReportResponse {
    private List<DetailedEmployeeJobworkResponse> jobworks;
    private OverallStats stats;

    /**
     * Aggregated statistics for the filtered jobworks.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverallStats {
        private Long totalJobworks;
        private Long totalIssuedQuantity;
        private Long totalAcceptedQuantity;
        private Long totalDamagedQuantity;
        private Long totalSalesQuantity;
        private Map<String, Long> damageBreakdown;
    }
}
