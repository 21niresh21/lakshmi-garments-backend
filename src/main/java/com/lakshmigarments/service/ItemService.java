package com.lakshmigarments.service;

import com.lakshmigarments.dto.ItemRequestDTO;
import com.lakshmigarments.dto.ItemResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ItemService {

    ItemResponseDTO createItem(ItemRequestDTO item);

    ItemResponseDTO updateItem(Long id, ItemRequestDTO itemRequestDTO);

    boolean deleteItem(Long id);

    List<ItemResponseDTO> getAllItems(String search);
}
