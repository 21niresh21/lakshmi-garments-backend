package com.lakshmigarments.service;

import java.util.List;
import java.util.Date;

import com.lakshmigarments.dto.BatchSerialDTO;
import com.lakshmigarments.dto.BatchTimelineResponse;
import com.lakshmigarments.dto.BatchDetailDTO;
import com.lakshmigarments.dto.BatchRequestDTO;
import com.lakshmigarments.dto.BatchResponseDTO;
import com.lakshmigarments.dto.BatchUpdateDTO;
import com.lakshmigarments.model.Batch;
import com.lakshmigarments.model.JobworkType;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface BatchService {

    void createBatch(BatchRequestDTO batchRequestDTO);

    List<BatchSerialDTO> getUnpackagedBatches();

    BatchTimelineResponse getBatchTimeline(Long batchId);

//    Long getBatchCount(Long batchId);

    Page<BatchResponseDTO> getAllBatches(Integer pageNo, Integer pageSize, String sortBy, String sortOrder,
            String search, List<String> batchStatusNames, 
            List<String> categoryNames, List<Boolean> isUrgents, Date startDate, Date endDate);

    void updateBatch(Long batchId, BatchUpdateDTO batchUpdateDTO);
    
    List<JobworkType> getAllowedJobworkTypes(String batchSerialCode);
    
    void recycleBatch(Long batchId);
    
    List<BatchDetailDTO> getBatchDetails(Long batchId);
    
    Long getAvailableQuantities(String serialCode, String jobworkType);
    
    Long getAvailableQuantitiesForCutting(String serialCode);

    Long getAvailableQuantitiesBySubCategory(String serialCode, String subCategoryName);
    
    List<String> getBatchSerialCodesForJobwork();
    
    void recalculateBatchStatus(Batch batch);
    
    List<String> getAllBatchSerialCode();

    List<String> getSubCategoriesBySerialCode(String serialCode);
    
}
