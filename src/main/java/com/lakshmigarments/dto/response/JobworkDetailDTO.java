package com.lakshmigarments.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.lakshmigarments.dto.JobworkItemDTO;
import com.lakshmigarments.dto.request.CreateJobworkReceiptItemRequest;
import com.lakshmigarments.model.JobworkItem;
import com.lakshmigarments.model.JobworkItemStatus;
import com.lakshmigarments.model.JobworkReceiptItem;
import com.lakshmigarments.model.JobworkType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobworkDetailDTO {

    private String jobworkNumber;
    
    private String jobworkOrigin;
    
    private String remarks;

    private String jobworkType;

    private String batchSerialCode;

    private String assignedTo;
    
    private String assignedBy;
    
    private LocalDateTime startedAt;
    
    private String jobworkStatus;
    
    private List<JobworkItemDTO> jobworkItems;
    
    private List<JobworkItemResponse> jobworkReceiptItems;


}
