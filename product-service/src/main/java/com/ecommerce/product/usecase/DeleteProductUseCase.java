package com.ecommerce.product.usecase;

import java.util.UUID;

public class DeleteProductUseCase {

    private final ProductRepository repository;

    public DeleteProductUseCase(ProductRepository repository) {
        this.repository = repository;
    }

    public boolean execute(UUID id) {
        if (repository.findById(id).isEmpty()) return false;
        repository.deleteById(id);
        return true;
    }
}
