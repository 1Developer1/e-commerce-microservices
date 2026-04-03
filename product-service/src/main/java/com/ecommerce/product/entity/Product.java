package com.ecommerce.product.entity;

import com.ecommerce.shared.domain.Money;
import java.util.UUID;

public class Product {

    private final UUID id;
    private String name;
    private String description;
    private Money price;
    private int stockQuantity;

    public Product(UUID id, String name, String description, Money price, int stockQuantity) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Product name is required");
        if (price == null) throw new IllegalArgumentException("Price is required");
        if (stockQuantity < 0) throw new IllegalArgumentException("Stock cannot be negative");
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public static Product create(String name, String description, Money price, int initialStock) {
        return new Product(UUID.randomUUID(), name, description, price, initialStock);
    }

    public void update(String name, String description, Money price, Integer stockQuantity) {
        if (name != null && !name.isBlank()) this.name = name;
        if (description != null) this.description = description;
        if (price != null) this.price = price;
        if (stockQuantity != null && stockQuantity >= 0) this.stockQuantity = stockQuantity;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (this.stockQuantity < quantity) throw new IllegalStateException("Not enough stock");
        this.stockQuantity -= quantity;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Money getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
}
