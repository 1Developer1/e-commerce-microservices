package com.ecommerce.product.usecase;

import com.ecommerce.product.entity.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ListProductsUseCase {

    private static final int MAX_SIZE = 100;
    private final ProductRepository repository;

    public ListProductsUseCase(ProductRepository repository) {
        this.repository = repository;
    }

    public record ProductSummary(UUID id, String name, BigDecimal price, String currency, int stock) {}
    public record Output(List<ProductSummary> products, int page, int size, long totalElements) {}

    public Output execute(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);

        List<ProductSummary> summaries = repository.findAll(safePage, safeSize).stream()
                .map(p -> new ProductSummary(p.getId(), p.getName(),
                        p.getPrice().getAmount(), p.getPrice().getCurrency(), p.getStockQuantity()))
                .toList();

        return new Output(summaries, safePage, safeSize, repository.count());
    }
}
