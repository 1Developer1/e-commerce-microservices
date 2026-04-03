package com.ecommerce.product.usecase;

import com.ecommerce.product.entity.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    void save(Product product);
    Optional<Product> findById(UUID id);
    List<Product> findAll(int page, int size);
    long count();
    void deleteById(UUID id);
}
