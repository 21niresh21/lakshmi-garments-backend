package com.lakshmigarments.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketResponseDTO {

    private String fileName;

    private String contentType;

    private byte[] fileData;
}
