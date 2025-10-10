package com.lakshmigarments.service;

import java.util.List;

import com.lakshmigarments.dto.BatchSerialDTO;
import com.lakshmigarments.dto.BatchTimelineDTO;
import com.lakshmigarments.dto.BatchRequestDTO;
import com.lakshmigarments.dto.BatchResponseDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface BatchService {

    void createBatch(BatchRequestDTO batchRequestDTO);

    List<BatchSerialDTO> getUnpackagedBatches();

    List<BatchTimelineDTO> getBatchTimeline(Long batchId);

    Long getBatchCount(Long batchId);

    Page<BatchResponseDTO> getAllBatches(Pageable pageable);

}
