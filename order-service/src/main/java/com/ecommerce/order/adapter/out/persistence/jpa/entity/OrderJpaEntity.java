package com.ecommerce.order.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id private UUID id;
    @Column(nullable = false) private UUID userId;
    @Column(name = "recipient_name", nullable = false) private String recipientName;
    @Column(name = "shipping_address", nullable = false) private String shippingAddress;
    @Column(name = "order_status", nullable = false) private String status;
    @Column(nullable = false) private BigDecimal discountAmount;
    @Column(nullable = false) private String discountCurrency;
    @Column(nullable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private BigDecimal totalAmount;
    @Column(nullable = false) private String currency;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItemJpaEntity> items = new ArrayList<>();

    public OrderJpaEntity() {}

    public void addItem(OrderItemJpaEntity item) { items.add(item); item.setOrder(this); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String v) { this.recipientName = v; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String v) { this.shippingAddress = v; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal v) { this.discountAmount = v; }
    public String getDiscountCurrency() { return discountCurrency; }
    public void setDiscountCurrency(String v) { this.discountCurrency = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal v) { this.totalAmount = v; }
    public String getCurrency() { return currency; }
    public void setCurrency(String v) { this.currency = v; }
    public List<OrderItemJpaEntity> getItems() { return items; }
    public void setItems(List<OrderItemJpaEntity> items) { this.items = items; }
}
