package com.lakshmigarments.service;

import java.util.List;

import com.lakshmigarments.dto.ItemResponseDTO;

public interface BatchItemService {

    List<ItemResponseDTO> getItemsByBatchId(Long batchId);
    
}
