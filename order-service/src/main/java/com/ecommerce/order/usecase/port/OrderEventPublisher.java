package com.ecommerce.order.usecase.port;

import com.ecommerce.shared.event.OrderPlacedEvent;

public interface OrderEventPublisher {
    void publishOrderPlaced(OrderPlacedEvent event);
}
