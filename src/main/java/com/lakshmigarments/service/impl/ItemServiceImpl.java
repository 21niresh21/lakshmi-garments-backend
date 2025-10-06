package com.lakshmigarments.service.impl;

import com.lakshmigarments.dto.ItemRequestDTO;
import com.lakshmigarments.dto.ItemResponseDTO;
import com.lakshmigarments.exception.DuplicateItemException;
import com.lakshmigarments.exception.ItemNotFoundException;
import com.lakshmigarments.model.Item;
import com.lakshmigarments.repository.ItemRepository;
import com.lakshmigarments.repository.specification.ItemSpecification;
import com.lakshmigarments.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemServiceImpl.class);
    private final ItemRepository itemRepository;
    private final ModelMapper modelMapper;

    @Override
    public ItemResponseDTO createItem(ItemRequestDTO item) {
        String itemName = item.getName().trim();

        if(itemRepository.existsByNameIgnoreCase(itemName)){
            LOGGER.error("Item already exists with name {}",itemName);
            throw new DuplicateItemException("Item already exists with name " + itemName);
        }

        Item newItem = new Item();
        newItem.setName(itemName);

        Item savedItem = itemRepository.save(newItem);
        LOGGER.debug("Item created with name {}", savedItem.getName());
        return modelMapper.map(savedItem, ItemResponseDTO.class);
    }

    @Override
    public ItemResponseDTO updateItem(Long id, ItemRequestDTO itemRequestDTO) {

        Item existingItem = itemRepository.findById(id).orElseThrow(
                ()-> {
                    LOGGER.error("Item not found with id {}", id);
                    return new ItemNotFoundException("Item not found with id " + id);
                }
        );

        String updatedName = itemRequestDTO.getName().trim();

        if(itemRepository.existsByNameIgnoreCase(updatedName)){
            LOGGER.error("Item already exists with name {}",updatedName);
            throw new DuplicateItemException("Item already exists with name " + updatedName);
        }

        existingItem.setName(updatedName);
        Item updatedItem = itemRepository.save(existingItem);
        LOGGER.debug("Item updated with id {}", updatedItem.getId());
        return modelMapper.map(updatedItem, ItemResponseDTO.class);
    }

    @Override
    public boolean deleteItem(Long id) {
        Item existingItem = itemRepository.findById(id).orElseThrow(
                ()->{
                    LOGGER.error("Item not found with id {}", id);
                    throw new ItemNotFoundException("Item not found with id " + id);
                }
        );

        long deletedCount = itemRepository.deleteByItem(existingItem);
        if(deletedCount > 0){
            LOGGER.debug("Item deleted with id {}", id);
            return true;
        } else {
            LOGGER.error("Failed to delete item with id {}", id);
            return false;
        }
    }

    @Override
    public List<ItemResponseDTO> getAllItems(String search) {
        Specification<Item> specification = ItemSpecification.filterByName(search);
        List<Item> items = itemRepository.findAll(specification);

        List<ItemResponseDTO> itemResponseDTOs = items.stream().map(item -> modelMapper.map(item, ItemResponseDTO.class)).collect(Collectors.toList());

        return itemResponseDTOs;
    }

}
