package com.lakshmigarments.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Root response DTO for the Jobwork Timeline API.
 * Encodes all jobwork details, child receipts, item-level progress,
 * and a chronological timeline of events.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobworkTimelineResponse {

    // ─── Jobwork Identity ─────────────────────────────────────
    private Long id;
    private String jobworkNumber;
    private String jobworkType;
    private String jobworkStatus;
    private String jobworkOrigin;
    private String batchSerialCode;
    private String assignedTo;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedAt;

    // ─── Parenting (if Split/Reassigned) ──────────────────────
    private String parentJobworkNumber;
    private List<String> childJobworkNumbers;

    // ─── Items Summary (Progress across all items) ────────────
    private List<JobworkItemSummary> items;

    // ─── Totals & Metrics ─────────────────────────────────────
    private JobworkMetrics metrics;

    // ─── Grouped Receipts ─────────────────────────────────────
    private List<ReceiptDetail> receipts;

    // ─── Chronological Timeline ───────────────────────────────
    private List<TimelineEvent> timeline;

    // ══════════════════════════════════════════════════════════
    //  INNER CLASSES
    // ══════════════════════════════════════════════════════════

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobworkItemSummary {
        private String itemName;
        private Long issuedQuantity;
        private Long acceptedQuantity;
        private Long damagedQuantity;
        private Long salesQuantity;
        private Long pendingQuantity;
        private String status;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobworkMetrics {
        private Long totalIssued;
        private Long totalAccepted;
        private Long totalDamaged;
        private Long totalSales;
        private Long totalPending;
        private Double totalWagesEarned;
        private Double totalSalesDeduction;
        private String completionPercentage; // e.g. "85%"
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiptDetail {
        private Long receiptId;
        private LocalDateTime receivedAt;
        private String recordedBy;
        private List<ReceiptItemDetail> receiptItems;
        private Long totalAccepted;
        private Long totalDamaged;
        private Long totalSales;
        private Double receiptWages;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiptItemDetail {
        private String itemName;
        private Long acceptedQuantity;
        private Long damagedQuantity;
        private Long salesQuantity;
        private Double wagePerItem;
        private Double salesPrice;
        private List<DamageDetail> damages;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DamageDetail {
        private Long quantity;
        private String damageType;
        private String reworkJobworkNumber;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineEvent {
        private TimelineEventType eventType;
        private String message;
        private LocalDateTime performedAt;
        private String performedBy;
        private String timeTakenFromPrevious;
        private String stage;

        // Stats snapshot for this specific event
        private Long quantityAffected;
        private List<TimelineItemDetail> items;
    }
}
