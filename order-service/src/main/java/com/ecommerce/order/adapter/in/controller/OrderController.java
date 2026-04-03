package com.ecommerce.order.adapter.in.controller;

import com.ecommerce.order.adapter.in.presenter.OrderPresenter;
import com.ecommerce.order.usecase.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final GetOrdersUseCase getOrdersUseCase;
    private final GetOrderByIdUseCase getOrderByIdUseCase;
    private final OrderPresenter presenter;

    public OrderController(PlaceOrderUseCase placeOrderUseCase,
                           GetOrdersUseCase getOrdersUseCase,
                           GetOrderByIdUseCase getOrderByIdUseCase,
                           OrderPresenter presenter) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.getOrdersUseCase = getOrdersUseCase;
        this.getOrderByIdUseCase = getOrderByIdUseCase;
        this.presenter = presenter;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> placeOrder(
            @RequestHeader("X-User-Id") String userIdStr,
            @Valid @RequestBody PlaceOrderRequest request) {

        UUID userId = UUID.fromString(userIdStr);
        var items = request.items().stream()
                .map(i -> new PlaceOrderUseCase.ItemInput(i.productId(), i.quantity()))
                .toList();

        var input = new PlaceOrderUseCase.Input(userId, request.recipientName(), request.shippingAddress(), items);
        var output = placeOrderUseCase.execute(input);

        if (!output.success()) {
            return ResponseEntity.badRequest().body(presenter.presentPlaceOrder(output));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(presenter.presentPlaceOrder(output));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrders(
            @RequestHeader("X-User-Id") String userIdStr,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID userId = UUID.fromString(userIdStr);
        var output = getOrdersUseCase.execute(new GetOrdersUseCase.Input(userId, page, size));
        return ResponseEntity.ok(presenter.presentGetOrders(output));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderById(
            @RequestHeader("X-User-Id") String userIdStr,
            @PathVariable UUID orderId) {

        UUID userId = UUID.fromString(userIdStr);
        var output = getOrderByIdUseCase.execute(new GetOrderByIdUseCase.Input(userId, orderId));
        if (!output.success()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(presenter.presentGetOrder(output));
        }
        return ResponseEntity.ok(presenter.presentGetOrder(output));
    }
}
