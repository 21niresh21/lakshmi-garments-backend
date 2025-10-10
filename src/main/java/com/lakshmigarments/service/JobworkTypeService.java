package com.lakshmigarments.service;

import java.util.List;

import com.lakshmigarments.dto.JobworkTypeResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface JobworkTypeService {

    List<JobworkTypeResponseDTO> getAllJobworkTypes();

}
