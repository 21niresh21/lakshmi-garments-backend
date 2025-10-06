package com.lakshmigarments.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.JobworkTypeResponseDTO;
import com.lakshmigarments.repository.JobworkTypeRepository;
import com.lakshmigarments.service.JobworkTypeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobworkTypeServiceImpl implements JobworkTypeService {

    private final Logger LOGGER = LoggerFactory.getLogger(JobworkTypeServiceImpl.class);
    private final JobworkTypeRepository jobworkTypeRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<JobworkTypeResponseDTO> getAllJobworkTypes() {
        LOGGER.debug("Fetching all jobwork types");
        return jobworkTypeRepository.findAll().stream()
                .map(jobworkType -> modelMapper.map(jobworkType, JobworkTypeResponseDTO.class))
                .collect(Collectors.toList());
    }
}
