package com.lakshmigarments.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatchTimelineDTO {

    private LocalDateTime dateTime;

    private String jobworkType;

    private String description;

    private String jobworkNumber;

}
