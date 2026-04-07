package com.lakshmigarments.service;

import java.util.List;
import com.lakshmigarments.dto.request.TransportRequest;
import com.lakshmigarments.dto.response.TransportResponse;

public interface TransportService {

    TransportResponse createTransport(TransportRequest transportRequest);

    List<TransportResponse> getAllTransports(String search);

    TransportResponse updateTransport(Long id, TransportRequest transportRequest);

}
