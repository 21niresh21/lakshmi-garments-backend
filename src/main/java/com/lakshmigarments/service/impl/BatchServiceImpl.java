package com.lakshmigarments.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.BatchSerialDTO;
import com.lakshmigarments.model.Batch;
import com.lakshmigarments.repository.BatchRepository;
import com.lakshmigarments.service.BatchService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {

    private final Logger LOGGER = LoggerFactory.getLogger(BatchServiceImpl.class);
    private final BatchRepository batchRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<BatchSerialDTO> getUnpackagedBatches() {
        LOGGER.info("Fetching unpackaged batches");
        List<Batch> unpackagedBatches = batchRepository.findUnpackagedBatches();
        List<BatchSerialDTO> batchSerialDTOs = unpackagedBatches.stream()
                .map(batch -> modelMapper.map(batch, BatchSerialDTO.class))
                .collect(Collectors.toList());
        LOGGER.info("Found {} unpackaged batches", batchSerialDTOs.size());
        return batchSerialDTOs;
    }

}
