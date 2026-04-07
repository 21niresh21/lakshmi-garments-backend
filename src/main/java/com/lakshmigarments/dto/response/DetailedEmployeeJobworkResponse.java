package com.lakshmigarments.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO representing detailed information for a single jobwork assigned to an employee.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailedEmployeeJobworkResponse {
    private String jobworkNumber;
    private String jobworkType;
    private String jobworkStatus;
    private String batchSerialCode;
    private LocalDateTime startedAt;
    private LocalDateTime lastUpdatedAt;
    private String remarks;
    private List<ItemDetail> items;

    /**
     * DTO for item-level details within a jobwork, including quantities and damage breakdown.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDetail {
        private String itemName;
        private Long issuedQuantity;
        private Long acceptedQuantity;
        private Long damagedQuantity;
        private Long salesQuantity;
        private Double salesPrice;
        private Double wagePerItem;
        private String status;
        private Map<String, Long> damageBreakdown;
    }
}
