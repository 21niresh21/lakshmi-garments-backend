package com.lakshmigarments.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobworkDetailDTO {

    private String jobworkNumber;

    private LocalDateTime startedAt;

    private String jobworkType;

    private String batchSerialCode;

    private String assignedTo;

    private List<String> items;

    private List<Long> quantity;

}
