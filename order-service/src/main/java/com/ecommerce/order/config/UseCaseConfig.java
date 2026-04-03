package com.ecommerce.order.config;

import com.ecommerce.order.adapter.in.presenter.OrderPresenter;
import com.ecommerce.order.usecase.*;
import com.ecommerce.order.usecase.port.OrderEventPublisher;
import com.ecommerce.order.usecase.port.ProductServicePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public PlaceOrderUseCase placeOrderUseCase(OrderRepository repo, ProductServicePort productService,
                                               OrderEventPublisher eventPublisher) {
        return new PlaceOrderUseCase(repo, productService, eventPublisher);
    }

    @Bean
    public GetOrdersUseCase getOrdersUseCase(OrderRepository repo) {
        return new GetOrdersUseCase(repo);
    }

    @Bean
    public GetOrderByIdUseCase getOrderByIdUseCase(OrderRepository repo) {
        return new GetOrderByIdUseCase(repo);
    }

    @Bean
    public OrderPresenter orderPresenter() {
        return new OrderPresenter();
    }
}
