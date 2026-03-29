package com.lakshmigarments.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for prior closed jobworks (used for damage source selection).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriorJobworkResponse {

    private String jobworkNumber;
    private String assignedTo;
    private String jobworkType;
    private LocalDateTime createdAt;
}
