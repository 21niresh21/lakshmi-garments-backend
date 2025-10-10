package com.lakshmigarments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class ItemRequestDTO {

    @NotBlank(message = "Item name is mandatory")
    @Size(max = 200)
    private String name;
}
