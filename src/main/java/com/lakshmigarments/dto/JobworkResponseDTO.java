package com.lakshmigarments.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.lakshmigarments.model.JobworkType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobworkResponseDTO {

	private Long id;
    private String assignedTo;
    private String batchSerial;
    private List<String> receivedItemNames;
    private List<Long> receivedQuantities;
    private List<String> issuedItemNames;
    private List<Long> issuedQuantities;
    private String jobworkType;
    private String jobworkNumber;
    private LocalDateTime startedAt;
    private List<LocalDateTime> completedAt;
    private String status;
    private Long totalQuantitesIssued;

}
