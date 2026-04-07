package com.lakshmigarments.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lakshmigarments.dto.request.ItemRequest;
import com.lakshmigarments.dto.response.ItemResponse;
import com.lakshmigarments.exception.DuplicateItemException;
import com.lakshmigarments.exception.ItemNotFoundException;
import com.lakshmigarments.model.Item;
import com.lakshmigarments.repository.ItemRepository;
import com.lakshmigarments.repository.specification.ItemSpecification;
import com.lakshmigarments.service.ItemService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ItemServiceImpl.class);

	private final ItemRepository itemRepository;
	private final ModelMapper modelMapper;

	@Override
	@Transactional(readOnly = true)
	public List<ItemResponse> getAllItems(String search) {
		LOGGER.debug("Fetching all items matching: {}", search);
		Specification<Item> spec = ItemSpecification.filterByName(search);
		List<Item> items = itemRepository.findAll(spec);

		LOGGER.debug("Found {} item(s)", items.size());
		return items.stream()
				.map(item -> modelMapper.map(item, ItemResponse.class))
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public ItemResponse createItem(ItemRequest itemRequest) {
		LOGGER.debug("Creating item: {}", itemRequest.getName());
		String itemName = itemRequest.getName().trim();

		validateItemUniqueness(itemName, null);

		Item item = new Item();
		item.setName(itemName);

		Item savedItem = itemRepository.save(item);
		LOGGER.info("Item created successfully with ID: {}", savedItem.getId());
		return modelMapper.map(savedItem, ItemResponse.class);
	}

	@Override
	@Transactional
	public ItemResponse updateItem(Long id, ItemRequest itemRequest) {
		LOGGER.debug("Updating item with ID: {}", id);
		
		Item item = this.getItemOrThrow(id);
		
		String itemName = itemRequest.getName().trim();

		validateItemUniqueness(itemName, id);

		item.setName(itemName);
		
		Item updatedItem = itemRepository.save(item);
		LOGGER.info("Item updated successfully with ID: {}", updatedItem.getId());
		return modelMapper.map(updatedItem, ItemResponse.class);
	}

	private void validateItemUniqueness(String name, Long id) {
		if (id == null) {
			if (itemRepository.existsByNameIgnoreCase(name)) {
				LOGGER.error("Item name already exists: {}", name);
				throw new DuplicateItemException("Item already exists with name: " + name);
			}
		} else {
			if (itemRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
				LOGGER.error("Item name already exists for another ID: {}", name);
				throw new DuplicateItemException("Item already exists with name: " + name);
			}
		}
	}

	private Item getItemOrThrow(Long id) {
		return itemRepository.findById(id).orElseThrow(() -> {
			LOGGER.error("Item not found with ID: {}", id);
			return new ItemNotFoundException("Item not found with ID: " + id);
		});
	}

}
