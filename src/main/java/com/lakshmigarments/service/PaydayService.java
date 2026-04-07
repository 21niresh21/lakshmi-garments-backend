package com.lakshmigarments.service;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.lakshmigarments.dto.PaydayDTO;

public interface PaydayService {

    Page<PaydayDTO> getAllPayday(
        String employeeName,
        LocalDateTime fromDate,
        LocalDateTime toDate,
        Pageable pageable
    );
}
