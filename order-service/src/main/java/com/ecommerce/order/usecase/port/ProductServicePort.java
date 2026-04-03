package com.ecommerce.order.usecase.port;

import com.ecommerce.shared.dto.ProductValidationResponse;
import java.util.UUID;

public interface ProductServicePort {
    ProductValidationResponse getProduct(UUID productId);
}
