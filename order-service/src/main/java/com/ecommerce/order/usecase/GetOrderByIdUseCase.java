package com.ecommerce.order.usecase;

import com.ecommerce.order.entity.Order;
import java.util.UUID;

public class GetOrderByIdUseCase {

    private final OrderRepository repository;

    public GetOrderByIdUseCase(OrderRepository repository) {
        this.repository = repository;
    }

    public record Input(UUID userId, UUID orderId) {}
    public record Output(boolean success, String message, Order order) {}

    public Output execute(Input input) {
        return repository.findById(input.orderId())
                .filter(order -> order.getUserId().equals(input.userId()))
                .map(order -> new Output(true, "Success", order))
                .orElse(new Output(false, "Order not found", null));
    }
}
