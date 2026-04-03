package com.ecommerce.order.usecase;

import com.ecommerce.order.entity.Order;
import java.util.List;
import java.util.UUID;

public class GetOrdersUseCase {

    private final OrderRepository repository;

    public GetOrdersUseCase(OrderRepository repository) {
        this.repository = repository;
    }

    public record Input(UUID userId, int page, int size) {}
    public record Output(boolean success, List<Order> orders) {
        public static Output success(List<Order> orders) { return new Output(true, orders); }
    }

    public Output execute(Input input) {
        List<Order> orders = repository.findByUserId(input.userId(), input.page(), input.size());
        return Output.success(orders);
    }
}
