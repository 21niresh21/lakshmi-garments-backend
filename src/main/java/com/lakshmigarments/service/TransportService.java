package com.lakshmigarments.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.TransportRequestDTO;
import com.lakshmigarments.dto.TransportResponseDTO;

@Service
public interface TransportService {

    TransportResponseDTO createTransport(TransportRequestDTO transportRequestDTO);

    List<TransportResponseDTO> getAllTransports(String search);

    TransportResponseDTO updateTransport(Long id, TransportRequestDTO transportRequestDTO);

}
