package com.ecommerce.order.entity;

import com.ecommerce.shared.domain.Money;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Order {

    public enum Status { CREATED, PAID, SHIPPED, DELIVERED, CANCELLED }

    private final UUID id;
    private final UUID userId;
    private final String recipientName;
    private final String shippingAddress;
    private final List<OrderItem> items;
    private final Money discount;
    private Status status;
    private final LocalDateTime createdAt;
    private final Money totalAmount;

    public Order(UUID id, UUID userId, String recipientName, String shippingAddress,
                 List<OrderItem> items, Money discount) {
        if (recipientName == null || recipientName.isBlank()) throw new IllegalArgumentException("Recipient name is required");
        if (shippingAddress == null || shippingAddress.isBlank()) throw new IllegalArgumentException("Shipping address is required");
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Order must have at least one item");
        this.id = id;
        this.userId = userId;
        this.recipientName = recipientName;
        this.shippingAddress = shippingAddress;
        this.items = items;
        this.discount = discount != null ? discount : new Money(BigDecimal.ZERO, "USD");
        this.status = Status.CREATED;
        this.createdAt = LocalDateTime.now();
        this.totalAmount = calculateTotal();
    }

    private Order(UUID id, UUID userId, String recipientName, String shippingAddress,
                  List<OrderItem> items, Money discount, Status status, LocalDateTime createdAt, Money totalAmount) {
        this.id = id;
        this.userId = userId;
        this.recipientName = recipientName;
        this.shippingAddress = shippingAddress;
        this.items = items;
        this.discount = discount != null ? discount : new Money(BigDecimal.ZERO, "USD");
        this.status = status;
        this.createdAt = createdAt;
        this.totalAmount = totalAmount;
    }

    public static Order create(UUID userId, String recipientName, String shippingAddress,
                               List<OrderItem> items, Money discount) {
        return new Order(UUID.randomUUID(), userId, recipientName, shippingAddress, items, discount);
    }

    public static Order restore(UUID id, UUID userId, String recipientName, String shippingAddress,
                                List<OrderItem> items, Money discount, Status status,
                                LocalDateTime createdAt, Money totalAmount) {
        return new Order(id, userId, recipientName, shippingAddress, items, discount, status, createdAt, totalAmount);
    }

    private Money calculateTotal() {
        Money subTotal = items.stream()
                .map(OrderItem::getSubTotal)
                .reduce(new Money(BigDecimal.ZERO, "USD"), Money::add);
        return subTotal.subtract(discount);
    }

    public void pay() {
        if (this.status != Status.CREATED) throw new IllegalStateException("Order already paid or cancelled");
        this.status = Status.PAID;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getRecipientName() { return recipientName; }
    public String getShippingAddress() { return shippingAddress; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public Money getDiscount() { return discount; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Money getTotalAmount() { return totalAmount; }
}
