package com.lakshmigarments.service;

import java.util.List;

import com.lakshmigarments.dto.response.BatchItemResponse;

public interface BatchItemService {

    List<BatchItemResponse> getBatchItemsByBatchSerial(String serialCode, String jobworkType);
    
}
