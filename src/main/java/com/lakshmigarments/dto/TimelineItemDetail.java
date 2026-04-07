package com.lakshmigarments.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimelineItemDetail {
    private String itemName;
    private Long quantity;
    private Long acceptedQuantity;
    private Long damagedQuantity;
    private Long salesQuantity;
}
