package com.ecommerce.product.adapter.out.persistence.jpa;

import com.ecommerce.product.adapter.out.persistence.jpa.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ProductSpringRepository extends JpaRepository<ProductJpaEntity, UUID> {
}
