package com.ecommerce.product.adapter.out.persistence.jpa;

import com.ecommerce.product.adapter.out.persistence.jpa.entity.ProductJpaEntity;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.usecase.ProductRepository;
import com.ecommerce.shared.domain.Money;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProductPersistenceAdapter implements ProductRepository {

    private final ProductSpringRepository springRepo;

    public ProductPersistenceAdapter(ProductSpringRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public void save(Product product) {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setId(product.getId());
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setPriceAmount(product.getPrice().getAmount());
        entity.setPriceCurrency(product.getPrice().getCurrency());
        entity.setStockQuantity(product.getStockQuantity());
        springRepo.save(entity);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return springRepo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Product> findAll(int page, int size) {
        return springRepo.findAll(PageRequest.of(page, size)).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public long count() {
        return springRepo.count();
    }

    @Override
    public void deleteById(UUID id) {
        springRepo.deleteById(id);
    }

    private Product toDomain(ProductJpaEntity e) {
        return new Product(e.getId(), e.getName(), e.getDescription(),
                Money.of(e.getPriceAmount(), e.getPriceCurrency()), e.getStockQuantity());
    }
}
