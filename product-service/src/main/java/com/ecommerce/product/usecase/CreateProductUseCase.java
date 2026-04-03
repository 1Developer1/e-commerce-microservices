package com.ecommerce.product.usecase;

import com.ecommerce.product.entity.Product;
import com.ecommerce.shared.domain.Money;
import java.math.BigDecimal;
import java.util.UUID;

public class CreateProductUseCase {

    private final ProductRepository repository;

    public CreateProductUseCase(ProductRepository repository) {
        this.repository = repository;
    }

    public record Input(String name, String description, BigDecimal priceAmount, String priceCurrency, int initialStock) {}
    public record Output(boolean success, String message, UUID id, String name) {}

    public Output execute(Input input) {
        Money price = Money.of(input.priceAmount(), input.priceCurrency());
        Product product = Product.create(input.name(), input.description(), price, input.initialStock());
        repository.save(product);
        return new Output(true, "Product created", product.getId(), product.getName());
    }
}
