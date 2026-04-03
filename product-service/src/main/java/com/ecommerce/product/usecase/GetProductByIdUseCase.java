package com.ecommerce.product.usecase;

import com.ecommerce.product.entity.Product;
import com.ecommerce.shared.dto.ProductValidationResponse;
import java.util.UUID;

public class GetProductByIdUseCase {

    private final ProductRepository repository;

    public GetProductByIdUseCase(ProductRepository repository) {
        this.repository = repository;
    }

    public ProductValidationResponse execute(UUID id) {
        return repository.findById(id)
                .map(p -> new ProductValidationResponse(
                        p.getId(), p.getName(),
                        p.getPrice().getAmount(), p.getPrice().getCurrency(),
                        p.getStockQuantity(), true))
                .orElse(ProductValidationResponse.notFound(id));
    }
}
