package com.lakshmigarments.model;

import lombok.Data;

@Data
public class JobworkPayload {
    private String batchId;
    private String jobType;
    private Integer quantity;
}
