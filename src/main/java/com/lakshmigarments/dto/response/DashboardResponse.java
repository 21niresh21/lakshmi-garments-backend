package com.lakshmigarments.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private List<KPI> weeklyKPIs;
    private List<ChartData> categoryData;
    private List<SubCategoryChartData> subCategoryData;
    private List<ChartData> supplierPerformanceData;
    private List<TrendData> quantityTrendData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KPI {
        private String title;
        private String value;
        private String type;
        private String subtitle;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartData {
        private String name;
        private Double value;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubCategoryChartData {
        private String name;
        private Double value;
        private String category;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private String day;
        private Long quantity;
    }
}
