package com.lakshmigarments.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.JobworkRequestDTO;
import com.lakshmigarments.dto.JobworkResponseDTO;
import com.lakshmigarments.dto.request.CreateJobworkRequest;
import com.lakshmigarments.dto.response.BatchItemResponse;
import com.lakshmigarments.dto.response.EmployeeJobworkReportResponse;
import com.lakshmigarments.dto.response.EmployeeJobworkResponse;
import com.lakshmigarments.dto.response.ItemResponse;
import com.lakshmigarments.dto.response.JobworkDetailDTO;
import com.lakshmigarments.dto.response.JobworkResponse;
import com.lakshmigarments.dto.JobworkTimelineResponse;
import com.lakshmigarments.model.Jobwork;
import com.lakshmigarments.model.JobworkStatus;
import com.lakshmigarments.model.JobworkType;

@Service
public interface JobworkService<T extends CreateJobworkRequest> {

    List<String> getJobworkNumbers(String search);

    JobworkDetailDTO getJobworkDetail(String jobworkNumber);

    String getNextJobworkNumber();



    Page<JobworkResponseDTO> getAllJobworks(
    	    Pageable pageable,
    	    String search, 
    	    List<String> assignedToNames, 
    	    List<JobworkStatus> statuses, 
    	    List<JobworkType> types, 
    	    List<String> batchSerialCodes,
    	    LocalDateTime startDate,
    	    LocalDateTime endDate
    	);
    
    Jobwork reAssignJobwork(String jobworkNumber, String employeeName);
    
    JobworkResponse createJobwork(T request);
    
    JobworkResponse closeJobwork(String jobworkNumber);
    
    JobworkResponse reopenJobwork(String jobworkNumber);
    
    List<ItemResponse> getItemsForJobwork(String jobworkNumber);
    
    List<EmployeeJobworkResponse> getJobworksByEmployeeName(String employeeName);
    
    EmployeeJobworkReportResponse getDetailedJobworksByEmployee(String employeeName, LocalDateTime startDate, LocalDateTime endDate);

    JobworkTimelineResponse getJobworkTimeline(String jobworkNumber);



}
