package com.lakshmigarments.dto;

import java.time.LocalDateTime;

import com.lakshmigarments.model.JobworkType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatchTimelineDTO {

    private LocalDateTime dateTime;

    private JobworkType jobworkType;

    private String description;

    private String jobworkNumber;

}
