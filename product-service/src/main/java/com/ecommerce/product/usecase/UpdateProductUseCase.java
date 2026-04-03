package com.ecommerce.product.usecase;

import com.ecommerce.product.entity.Product;
import com.ecommerce.shared.domain.Money;
import java.math.BigDecimal;
import java.util.UUID;

public class UpdateProductUseCase {

    private final ProductRepository repository;

    public UpdateProductUseCase(ProductRepository repository) {
        this.repository = repository;
    }

    public record Output(boolean success, String message, UUID id) {}

    public Output execute(UUID id, String name, String description, BigDecimal priceAmount, String priceCurrency, Integer stock) {
        return repository.findById(id)
                .map(product -> {
                    Money price = (priceAmount != null && priceCurrency != null) ? Money.of(priceAmount, priceCurrency) : null;
                    product.update(name, description, price, stock);
                    repository.save(product);
                    return new Output(true, "Product updated", product.getId());
                })
                .orElse(new Output(false, "Product not found", id));
    }
}
