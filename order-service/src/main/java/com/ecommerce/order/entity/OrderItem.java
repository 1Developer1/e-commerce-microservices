package com.ecommerce.order.entity;

import com.ecommerce.shared.domain.Money;
import java.util.UUID;

public class OrderItem {

    private final UUID productId;
    private final String productName;
    private final int quantity;
    private final Money price;

    public OrderItem(UUID productId, String productName, int quantity, Money price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public Money getSubTotal() { return price.multiply(quantity); }
    public UUID getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public Money getPrice() { return price; }
}
