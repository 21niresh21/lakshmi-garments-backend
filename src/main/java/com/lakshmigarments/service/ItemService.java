package com.lakshmigarments.service;

import java.util.List;
import com.lakshmigarments.dto.request.ItemRequest;
import com.lakshmigarments.dto.response.ItemResponse;

public interface ItemService {

    ItemResponse createItem(ItemRequest itemRequest);

    ItemResponse updateItem(Long id, ItemRequest itemRequest);

    List<ItemResponse> getAllItems(String search);

}
