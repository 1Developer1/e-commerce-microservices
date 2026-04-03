package com.ecommerce.order.adapter.in.controller;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record PlaceOrderRequest(
        @NotBlank(message = "Recipient name is required") String recipientName,
        @NotBlank(message = "Shipping address is required") String shippingAddress,
        @NotEmpty(message = "At least one item is required") List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            @NotNull UUID productId,
            @Positive int quantity
    ) {}
}
