package com.ecommerce.order.adapter.out.persistence.jpa;

import com.ecommerce.order.adapter.out.persistence.jpa.entity.OrderJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderSpringRepository extends JpaRepository<OrderJpaEntity, UUID> {
    List<OrderJpaEntity> findByUserId(UUID userId, Pageable pageable);
}
