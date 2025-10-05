package com.lakshmigarments.service;

import java.util.List;

import com.lakshmigarments.dto.BatchSerialDTO;
import org.springframework.stereotype.Service;

@Service
public interface BatchService {
    List<BatchSerialDTO> getUnpackagedBatches();
}
