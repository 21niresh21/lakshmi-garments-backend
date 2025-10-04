package com.lakshmigarments.controller;

import com.lakshmigarments.dto.ItemRequestDTO;
import com.lakshmigarments.dto.ItemResponseDTO;
import com.lakshmigarments.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class ItemController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemController.class);

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<List<ItemResponseDTO>> getAllItems(@RequestParam(required = false) String search){
        LOGGER.info("Received request to get all items");
        List<ItemResponseDTO> itemResponseDTO = itemService.getAllItems(search);
        LOGGER.info("Items retrieved successfully");
        return ResponseEntity.status(HttpStatus.OK).body(itemResponseDTO);
    }

    @PostMapping
    public ResponseEntity<ItemResponseDTO> createItem(@Valid @RequestBody ItemRequestDTO itemRequestDTO){
        LOGGER.info("Received request to create item: {}", itemRequestDTO.getName());
        ItemResponseDTO itemResponseDTO = itemService.createItem(itemRequestDTO);
        LOGGER.info("Item created successfully with ID: {}", itemResponseDTO.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(itemResponseDTO);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemResponseDTO> updateItem(@PathVariable Long id, @Valid @RequestBody ItemRequestDTO itemRequestDTO){
        LOGGER.info("Received request to update item with ID: {}", id);
        ItemResponseDTO itemResponseDTO = itemService.updateItem(id, itemRequestDTO);
        LOGGER.info("Item updated successfully with ID: {}", itemResponseDTO.getId());
        return ResponseEntity.status(HttpStatus.OK).body(itemResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteItem(@PathVariable Long id){
        LOGGER.info("Received request to delete item with ID: {}", id);
        boolean isDeleted = itemService.deleteItem(id);
        if(isDeleted){
            LOGGER.info("Item deleted successfully with ID: {}", id);
            return ResponseEntity.status(HttpStatus.OK).body(true);
        } else {
            LOGGER.warn("Item not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
    }
}
