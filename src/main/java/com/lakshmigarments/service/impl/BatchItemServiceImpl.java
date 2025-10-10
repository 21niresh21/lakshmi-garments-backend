package com.lakshmigarments.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.ItemResponseDTO;
import com.lakshmigarments.model.BatchItem;
import com.lakshmigarments.service.BatchItemService;
import com.lakshmigarments.repository.BatchItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatchItemServiceImpl implements BatchItemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchItemServiceImpl.class);
    private final BatchItemRepository batchItemRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<ItemResponseDTO> getItemsByBatchId(Long batchId) {
        LOGGER.debug("Fetching items by batch id: {}", batchId);
        List<BatchItem> batchItems = batchItemRepository.findByBatchId(batchId);
        return batchItems.stream().map(batchItem -> modelMapper.map(batchItem, ItemResponseDTO.class))
                .collect(Collectors.toList());
    }

}
