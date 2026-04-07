package com.lakshmigarments.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Root response DTO for the Batch Timeline API.
 * Encodes all batch details, quantity flow, jobwork lifecycle,
 * and a chronological timeline of events.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTimelineResponse {

    // ─── Batch Identity ───────────────────────────────────────
    private Long batchId;
    private String serialCode;
    private String categoryName;
    private String batchStatus;
    private Boolean isUrgent;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedAt;

    // ─── Batch Items (what the batch contains) ────────────────
    private List<BatchItemSummary> items;

    // ─── Batch Sub-Categories ─────────────────────────────────
    private List<SubCategorySummary> subCategories;

    // ─── Stage Progress ───────────────────────────────────────
    private StageProgress cuttingProgress;
    private StageProgress embroideryProgress;
    private StageProgress stitchingProgress;
    private StageProgress packagingProgress;

    // ─── Quantity Flow (aggregate numbers) ────────────────────
    private QuantityFlow quantityFlow;

    // ─── Jobwork Summary (grouped by jobwork) ─────────────────
    private List<JobworkSummary> jobworks;

    // ─── Chronological Timeline Events ────────────────────────
    private List<TimelineEvent> timeline;

    // ─── Aggregate Stats ──────────────────────────────────────
    private TimelineStats stats;

    // ══════════════════════════════════════════════════════════
    //  INNER CLASSES
    // ══════════════════════════════════════════════════════════

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchItemSummary {
        private Long itemId;
        private String itemName;
        private Long quantity;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StageProgress {
        private Long totalQuantity;
        private Long completedQuantity;
        private LocalDateTime firstStartedAt;
        private String progressPercentage;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubCategorySummary {
        private Long id;
        private String subCategoryName;
        private Long originalQuantity;
        private Long availableQuantity;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuantityFlow {
        private PreCuttingFlow preCutting;
        private PostCuttingFlow postCutting;
        private Long currentAvailableQuantity; // Total quantity currently in the batch (Post-cutting)
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreCuttingFlow {
        private Long totalQuantity;      // Total Original Sub-Category Quantity
        private Long assignedQuantity;   // Total Quantity assigned to CUTTING jobworks
        private Long consumedQuantity;   // Total Quantity accepted/consumed from CUTTING receipts
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostCuttingFlow {
        private Long totalQuantity;      // Total Items Produced from CUTTING
        private Long assignedQuantity;   // Total Items assigned to subsequent jobworks (STITCHING, etc.)
        private Long acceptedQuantity;   // Total Items accepted back from receipts
        private Long damagedQuantity;    // Total Items damaged
        private Long salesQuantity;      // Total Items sold
        private Long repairableDamage;
        private Long unrepairableDamage;
        private Long supplierDamage;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobworkSummary {
        private Long jobworkId;
        private String jobworkNumber;
        private String jobworkType;       // CUTTING / STITCHING / PACKAGING
        private String jobworkOrigin;     // ORIGINAL / SPLIT / REASSIGNED
        private String jobworkStatus;
        private String assignedTo;
        private String remarks;
        private LocalDateTime assignedAt;
        private String createdBy;

        // Parent jobwork reference (for SPLIT/REASSIGNED)
        private String parentJobworkNumber;

        // Items assigned in this jobwork
        private List<JobworkItemDetail> assignedItems;

        // Receipts under this jobwork
        private List<ReceiptSummary> receipts;

        // Totals for this jobwork
        private Long totalAssignedQuantity;
        private Long totalAcceptedQuantity;
        private Long totalDamagedQuantity;
        private Long totalSalesQuantity;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobworkItemDetail {
        private String itemName;
        private Long quantity;
        private String itemStatus; // IN_PROGRESS, CLOSED, etc.
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiptSummary {
        private Long receiptId;
        private LocalDateTime receivedAt;
        private String receivedBy;
        private List<ReceiptItemDetail> receiptItems;
        private Long totalAccepted;
        private Long totalDamaged;
        private Long totalSales;
        private Double totalWages;
        private Double totalSalesAmount;
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
        private Double salesPrice;
        private Double wagePerItem;
        private List<DamageDetail> damages;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DamageDetail {
        private Long quantity;
        private String damageType;         // REPAIRABLE / UNREPAIRABLE / SUPPLIER_DAMAGE
        private String reworkJobworkNumber; // if damage was reworked
    }

    // ─── Timeline Event ───────────────────────────────────────

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

        // Contextual references (nullable based on event type)
        private String jobworkNumber;
        private String jobworkType;
        private String employeeName;

        // Quantity snapshot at this event
        private Long totalQuantity;
        private Long acceptedQuantity;
        private Long damagedQuantity;
        private Long salesQuantity;

        // Item-level breakdown for this event
        private List<TimelineItemDetail> items;
    }

    // ─── Aggregate Stats ──────────────────────────────────────

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineStats {
        private Integer totalEvents;
        private Integer totalJobworks;
        private Integer totalReceipts;
        private String totalDurationFromCreation;  // human-readable e.g. "3 days 5 hours"
        private LocalDateTime firstEventAt;
        private LocalDateTime lastEventAt;
        private String totalDurationFromItemCreation;
        
        // ─── Additional Performance Metrics ─────────────────────
        private Integer cuttingJobworkCount;
        private Integer stitchingJobworkCount;
        private Integer packagingJobworkCount;
        private Integer uniqueEmployeesAssigned;    // Number of unique employees who worked on this batch
        private String averageTimeBetweenJobworks;
        private String averageTimeBetweenReceipts;
        private Double totalWagesPaid;
        private Double totalSalesRevenue;
        private Double totalCostOfProduction;
        private Long totalItemsProduced;            // Items from cutting
        private Long totalItemsAccepted;             // Final accepted items
        private Long totalItemsDamaged;
        private Long totalItemsSold;
        private Double overallAcceptanceRate;       // accepted / totalReturned * 100
        private Double overallDamageRate;            // damaged / totalReturned * 100
        private Double overallSalesRate;             // sales / totalReturned * 100
        private String productionEfficiencyScore;    // A/B/C/D rating
        private Long totalReworkCount;               // Items sent for rework
        private String estimatedCompletionTime;      // Estimated time to complete remaining work
    }
}
