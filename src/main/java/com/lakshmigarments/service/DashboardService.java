package com.lakshmigarments.service;

import com.lakshmigarments.dto.response.DashboardResponse;
import java.time.LocalDate;

public interface DashboardService {
    DashboardResponse getDashboardData(LocalDate startDate, LocalDate endDate);
}
