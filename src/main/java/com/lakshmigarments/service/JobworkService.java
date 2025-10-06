package com.lakshmigarments.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.JobworkDetailDTO;
import com.lakshmigarments.dto.JobworkRequestDTO;
import com.lakshmigarments.dto.JobworkResponseDTO;

@Service
public interface JobworkService {

    List<String> getJobworkNumbers(String search);

    JobworkDetailDTO getJobworkDetail(String jobworkNumber);

    String getNextJobworkNumber();

    void createJobwork(JobworkRequestDTO jobworkRequestDTO);

    Page<JobworkResponseDTO> getAllJobworks(Pageable pageable, String search);

}
