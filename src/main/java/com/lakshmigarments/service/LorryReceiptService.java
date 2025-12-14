package com.lakshmigarments.service;

import com.lakshmigarments.dto.LorryReceiptUpdateDTO;

public interface LorryReceiptService {

    void updateLorryReceipt(Long id, LorryReceiptUpdateDTO lorryReceiptUpdateDTO);
    
}
