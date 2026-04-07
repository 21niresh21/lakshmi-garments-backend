package com.lakshmigarments.service;

import java.util.List;
import com.lakshmigarments.dto.request.SupplierRequest;
import com.lakshmigarments.dto.response.SupplierResponse;

public interface SupplierService {

    SupplierResponse createSupplier(SupplierRequest supplierRequest);

    SupplierResponse updateSupplier(Long id, SupplierRequest supplierRequest);

    List<SupplierResponse> getSuppliers(String search);

}
