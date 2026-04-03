package com.ecommerce.order.usecase;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.usecase.port.OrderEventPublisher;
import com.ecommerce.order.usecase.port.ProductServicePort;
import com.ecommerce.shared.domain.Money;
import com.ecommerce.shared.dto.ProductValidationResponse;
import com.ecommerce.shared.event.OrderPlacedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class PlaceOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(PlaceOrderUseCase.class);
    private final OrderRepository orderRepository;
    private final ProductServicePort productService;
    private final OrderEventPublisher eventPublisher;

    public PlaceOrderUseCase(OrderRepository orderRepository,
                             ProductServicePort productService,
                             OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.eventPublisher = eventPublisher;
    }

    public record Input(UUID userId, String recipientName, String shippingAddress, List<ItemInput> items) {}
    public record ItemInput(UUID productId, int quantity) {}
    public record Output(boolean success, String message, UUID orderId, String status, BigDecimal totalAmount) {}

    public Output execute(Input input) {
        if (input.items() == null || input.items().isEmpty()) {
            return new Output(false, "Order must have at least one item", null, null, null);
        }

        List<OrderItem> orderItems = new ArrayList<>();
        Map<UUID, Integer> productQuantities = new HashMap<>();

        for (ItemInput item : input.items()) {
            ProductValidationResponse product = productService.getProduct(item.productId());
            if (!product.exists()) {
                return new Output(false, "Product not found: " + item.productId(), null, null, null);
            }

            Money price = Money.of(product.priceAmount(), product.priceCurrency());
            orderItems.add(new OrderItem(product.id(), product.name(), item.quantity(), price));
            productQuantities.put(product.id(), item.quantity());
        }

        Order order = Order.create(input.userId(), input.recipientName(), input.shippingAddress(),
                orderItems, new Money(BigDecimal.ZERO, "USD"));

        orderRepository.save(order);

        try {
            eventPublisher.publishOrderPlaced(new OrderPlacedEvent(
                    order.getId(), productQuantities, LocalDateTime.now()));
        } catch (Exception e) {
            // Order is saved but event failed — log for retry/outbox pattern later
            log.warn("Order {} saved but event publish failed: {}", order.getId(), e.getMessage());
        }

        return new Output(true, "Order placed successfully",
                order.getId(), order.getStatus().name(), order.getTotalAmount().getAmount());
    }
}
