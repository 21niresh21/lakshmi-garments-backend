package com.lakshmigarments.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.JobworkDetailDTO;

@Service
public interface JobworkService {

    List<String> getJobworkNumbers(String search);

    JobworkDetailDTO getJobworkDetail(String jobworkNumber);

    String getNextJobworkNumber();

}
