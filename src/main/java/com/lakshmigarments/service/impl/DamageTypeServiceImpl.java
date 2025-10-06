package com.lakshmigarments.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.DamageTypeResponseDTO;
import com.lakshmigarments.repository.DamageTypeRepository;
import com.lakshmigarments.service.DamageTypeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DamageTypeServiceImpl implements DamageTypeService {

    private final Logger LOGGER = LoggerFactory.getLogger(DamageTypeServiceImpl.class);
    private final DamageTypeRepository damageTypeRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<DamageTypeResponseDTO> getAllDamageTypes() {
        LOGGER.debug("Fetching all damage types");
        return damageTypeRepository.findAll().stream()
                .map(damageType -> modelMapper.map(damageType, DamageTypeResponseDTO.class))
                .collect(Collectors.toList());
    }

}
