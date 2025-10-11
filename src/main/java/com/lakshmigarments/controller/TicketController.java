package com.lakshmigarments.controller;

import com.itextpdf.text.DocumentException;
import com.lakshmigarments.dto.TicketRequestDTO;
import com.lakshmigarments.dto.TicketResponseDTO;
import com.lakshmigarments.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/ticket")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class TicketController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketController.class);
    private final TicketService ticketService;

//    @PostMapping
//    ResponseEntity<TicketResponseDTO> getTicket(@RequestBody @Valid TicketRequestDTO ticketRequestDTO) throws DocumentException, IOException {
//        String jobworkNumber = ticketRequestDTO.getJobworksList().stream().findFirst().get().getJobworkNumber();
//        LOGGER.info("Received request to generate ticket for JobWork ID: {}", jobworkNumber);
//        TicketResponseDTO ticketResponseDTO = ticketService.generateTicket(ticketRequestDTO);
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_PDF);
//        headers.setContentDispositionFormData("attachment", ticketResponseDTO.getFileName());
//        LOGGER.info("Ticket generated successfully for JobWork ID: {}", ticketResponseDTO.getFileName());
//        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(ticketResponseDTO);
//    }

    @PostMapping
    ResponseEntity<byte[]> getTicket(@RequestBody @Valid TicketRequestDTO ticketRequestDTO) throws DocumentException, IOException {
        String jobworkNumber = ticketRequestDTO.getJobworksList().stream().findFirst().get().getJobworkNumber();
        LOGGER.info("Received request to generate ticket for JobWork ID: {}", jobworkNumber);
        byte[] ticket = ticketService.generateTicketBytes(ticketRequestDTO);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", jobworkNumber+".pdf");
        LOGGER.info("Ticket generated successfully for JobWork ID: {}", jobworkNumber);
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(ticket);
    }
}
