package com.ecommerce.product.usecase;

import com.ecommerce.product.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.UUID;

public class DeductProductStockUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeductProductStockUseCase.class);
    private final ProductRepository repository;

    public DeductProductStockUseCase(ProductRepository repository) {
        this.repository = repository;
    }

    public void execute(Map<UUID, Integer> productQuantities) {
        for (var entry : productQuantities.entrySet()) {
            UUID productId = entry.getKey();
            int quantity = entry.getValue();

            Product product = repository.findById(productId)
                    .orElseThrow(() -> new IllegalStateException("Product not found: " + productId));

            product.decreaseStock(quantity);
            repository.save(product);
            log.info("Stock deducted: product={}, quantity={}, remaining={}", productId, quantity, product.getStockQuantity());
        }
    }
}
