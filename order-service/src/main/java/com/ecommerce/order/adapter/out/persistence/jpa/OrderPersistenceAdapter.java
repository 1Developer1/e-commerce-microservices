package com.ecommerce.order.adapter.out.persistence.jpa;

import com.ecommerce.order.adapter.out.persistence.jpa.entity.*;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.usecase.OrderRepository;
import com.ecommerce.shared.domain.Money;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OrderPersistenceAdapter implements OrderRepository {

    private final OrderSpringRepository springRepo;

    public OrderPersistenceAdapter(OrderSpringRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public void save(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(order.getId());
        entity.setUserId(order.getUserId());
        entity.setRecipientName(order.getRecipientName());
        entity.setShippingAddress(order.getShippingAddress());
        entity.setStatus(order.getStatus().name());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setTotalAmount(order.getTotalAmount().getAmount());
        entity.setCurrency(order.getTotalAmount().getCurrency());
        entity.setDiscountAmount(order.getDiscount().getAmount());
        entity.setDiscountCurrency(order.getDiscount().getCurrency());

        order.getItems().forEach(item -> {
            OrderItemJpaEntity ie = new OrderItemJpaEntity();
            ie.setProductId(item.getProductId());
            ie.setProductName(item.getProductName());
            ie.setQuantity(item.getQuantity());
            ie.setPriceAmount(item.getPrice().getAmount());
            ie.setPriceCurrency(item.getPrice().getCurrency());
            entity.addItem(ie);
        });

        springRepo.save(entity);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return springRepo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Order> findByUserId(UUID userId, int page, int size) {
        return springRepo.findByUserId(userId, PageRequest.of(page, size)).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    private Order toDomain(OrderJpaEntity e) {
        List<OrderItem> items = e.getItems().stream()
                .map(i -> new OrderItem(i.getProductId(), i.getProductName(), i.getQuantity(),
                        Money.of(i.getPriceAmount(), i.getPriceCurrency())))
                .collect(Collectors.toList());

        return Order.restore(e.getId(), e.getUserId(), e.getRecipientName(), e.getShippingAddress(),
                items, Money.of(e.getDiscountAmount(), e.getDiscountCurrency()),
                Order.Status.valueOf(e.getStatus()), e.getCreatedAt(),
                Money.of(e.getTotalAmount(), e.getCurrency()));
    }
}
