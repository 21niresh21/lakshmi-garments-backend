package com.lakshmigarments.service.impl;

import com.itextpdf.text.DocumentException;
import com.lakshmigarments.dto.TicketRequestDTO;
import com.lakshmigarments.dto.TicketResponseDTO;
import com.lakshmigarments.exception.JobworkNotFoundException;
import com.lakshmigarments.model.Jobwork;
import com.lakshmigarments.repository.JobworkRepository;
import com.lakshmigarments.service.TicketService;
import com.lakshmigarments.utility.TicketHelper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketHelper ticketHelper;
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketServiceImpl.class);

    @Override
    public TicketResponseDTO generateTicket(TicketRequestDTO ticketRequestDTO) throws DocumentException, IOException {
        String jobworkNumber = ticketRequestDTO.getJobworksList().stream().findFirst().get().getJobworkNumber();
        LOGGER.info("Generating ticket for JobWork Number: {}", jobworkNumber);
        String bodyContent = prepareBodyContent(jobworkNumber,ticketRequestDTO);
        ticketHelper.prepareDocument(jobworkNumber);
        ticketHelper.addContent(ticketHelper.getDocument(),bodyContent,jobworkNumber);
        byte[] ticket = ticketHelper.generateTicket();
        LOGGER.info("Ticket generated successfully for JobWork Number: {}", jobworkNumber);
        return prepareTicketResponse(jobworkNumber,ticket);
    }

    @Override
    public byte[] generateTicketBytes(TicketRequestDTO ticketRequestDTO) throws DocumentException, IOException {
        String jobworkNumber = ticketRequestDTO.getJobworksList().stream().findFirst().get().getJobworkNumber();
        LOGGER.info("Generating ticket for JobWork Number: {}", jobworkNumber);
        String bodyContent = prepareBodyContent(jobworkNumber,ticketRequestDTO);
        ticketHelper.prepareDocument(jobworkNumber);
        ticketHelper.addContent(ticketHelper.getDocument(),bodyContent,jobworkNumber);
        byte[] ticket = ticketHelper.generateTicket();
        LOGGER.info("Ticket generated successfully for JobWork Number: {}", jobworkNumber);
        return ticket;
    }

    private String prepareBodyContent(String jobworkNumber,TicketRequestDTO ticketRequestDTO){
        StringBuilder bodyContent = new StringBuilder();
        bodyContent.append(jobworkNumber).append("\n");
        ticketRequestDTO.getJobworksList().stream().forEach((jobworkDetailDTO)-> {
                LOGGER.info("Working for Jobwork : {}",jobworkDetailDTO.toString());
                if(jobworkDetailDTO.getItems()!=null){
                    bodyContent.append("Items: ").append(String.join(", ", jobworkDetailDTO.getItems())).append("\n");
                }
                bodyContent.append("Quantity: ").append(jobworkDetailDTO.getQuantity()).append("\n")
                .append("By: ").append(jobworkDetailDTO.getAssignedTo()).append("\n")
                .append("Batch: ").append(jobworkDetailDTO.getBatchSerialCode()).append("\n")
                .append("------------------------------").append("\n");});

        return bodyContent.toString();
    }

    private TicketResponseDTO prepareTicketResponse(String jobworkNumber, byte[] ticket){
        TicketResponseDTO ticketResponseDTO = new TicketResponseDTO();
        ticketResponseDTO.setFileName(jobworkNumber+".pdf");
        ticketResponseDTO.setContentType("application/pdf");
        ticketResponseDTO.setFileData(ticket);
        return ticketResponseDTO;
    }
}
