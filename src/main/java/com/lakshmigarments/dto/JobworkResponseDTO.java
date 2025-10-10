package com.lakshmigarments.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobworkResponseDTO {

    private String employeeName;
    private String batchSerial;
    private List<String> itemNames;
    private List<Long> quantities;
    private String jobworktype;
    private String jobworkNumber;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String status;

}
