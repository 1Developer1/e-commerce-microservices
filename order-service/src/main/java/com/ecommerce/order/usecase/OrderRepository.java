package com.ecommerce.order.usecase;

import com.ecommerce.order.entity.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(UUID id);
    List<Order> findByUserId(UUID userId, int page, int size);
}
