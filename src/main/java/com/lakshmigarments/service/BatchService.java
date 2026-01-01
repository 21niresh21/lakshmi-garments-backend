package com.lakshmigarments.service;

import java.util.List;
import java.util.Date;

import com.lakshmigarments.dto.BatchSerialDTO;
import com.lakshmigarments.dto.BatchTimelineDTO;
import com.lakshmigarments.dto.BatchRequestDTO;
import com.lakshmigarments.dto.BatchResponseDTO;
import com.lakshmigarments.dto.BatchUpdateDTO;
import com.lakshmigarments.model.JobworkType;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface BatchService {

    void createBatch(BatchRequestDTO batchRequestDTO);

    List<BatchSerialDTO> getUnpackagedBatches();

//    List<BatchTimelineDTO> getBatchTimeline(Long batchId);

//    Long getBatchCount(Long batchId);

    Page<BatchResponseDTO> getAllBatches(Integer pageNo, Integer pageSize, String sortBy, String sortOrder,
            String search, List<String> batchStatusNames, 
            List<String> categoryNames, List<Boolean> isUrgents, Date startDate, Date endDate);

    void updateBatch(Long batchId, BatchUpdateDTO batchUpdateDTO);
    
    List<JobworkType> getJobworkTypes(String batchSerialCode);
    
    void recycleBatch(Long batchId);
    
}
