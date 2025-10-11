package com.lakshmigarments.service;

import com.itextpdf.text.DocumentException;
import com.lakshmigarments.dto.TicketRequestDTO;
import com.lakshmigarments.dto.TicketResponseDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface TicketService {
    TicketResponseDTO generateTicket(TicketRequestDTO ticketRequestDTO) throws DocumentException, IOException;
    byte[] generateTicketBytes(TicketRequestDTO ticketRequestDTO) throws DocumentException, IOException;
}
