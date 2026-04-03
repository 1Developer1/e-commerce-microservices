package com.ecommerce.order.adapter.in.presenter;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.usecase.GetOrderByIdUseCase;
import com.ecommerce.order.usecase.GetOrdersUseCase;
import com.ecommerce.order.usecase.PlaceOrderUseCase;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class OrderPresenter {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public Map<String, Object> presentPlaceOrder(PlaceOrderUseCase.Output output) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("success", output.success());
        map.put("message", output.message());
        if (output.success() && output.orderId() != null) {
            map.put("orderId", output.orderId().toString());
            map.put("status", output.status());
            map.put("totalAmount", output.totalAmount().toPlainString());
        }
        return map;
    }

    public Map<String, Object> presentGetOrders(GetOrdersUseCase.Output output) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("orders", output.orders().stream().map(this::orderToMap).collect(Collectors.toList()));
        return map;
    }

    public Map<String, Object> presentGetOrder(GetOrderByIdUseCase.Output output) {
        if (!output.success() || output.order() == null) {
            return Map.of("error", output.message());
        }
        return orderToMap(output.order());
    }

    private Map<String, Object> orderToMap(Order order) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", order.getId().toString());
        map.put("status", order.getStatus().name());
        map.put("recipientName", order.getRecipientName());
        map.put("shippingAddress", order.getShippingAddress());
        map.put("createdAt", order.getCreatedAt().format(FMT));
        if (order.getTotalAmount() != null) {
            map.put("totalAmount", order.getTotalAmount().getAmount().toPlainString());
            map.put("currency", order.getTotalAmount().getCurrency());
        }
        List<Map<String, Object>> items = order.getItems().stream().map(item -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("productId", item.getProductId().toString());
            m.put("productName", item.getProductName());
            m.put("quantity", item.getQuantity());
            m.put("price", item.getPrice().getAmount().toPlainString());
            return m;
        }).collect(Collectors.toList());
        map.put("items", items);
        return map;
    }
}
