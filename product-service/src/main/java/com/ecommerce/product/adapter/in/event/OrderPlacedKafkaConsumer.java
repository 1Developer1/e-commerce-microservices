package com.ecommerce.product.adapter.in.event;

import com.ecommerce.product.usecase.DeductProductStockUseCase;
import com.ecommerce.shared.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPlacedKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderPlacedKafkaConsumer.class);
    private final DeductProductStockUseCase deductStockUseCase;

    public OrderPlacedKafkaConsumer(DeductProductStockUseCase deductStockUseCase) {
        this.deductStockUseCase = deductStockUseCase;
    }

    @KafkaListener(topics = "order-placed-events", groupId = "product-service-stock-group")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("[Kafka Consumer] Received OrderPlacedEvent for orderId={}", event.orderId());
        try {
            deductStockUseCase.execute(event.productQuantities());
            log.info("[Kafka Consumer] Stock deducted successfully for orderId={}", event.orderId());
        } catch (Exception e) {
            log.error("[Kafka Consumer] Failed to deduct stock for orderId={}: {}", event.orderId(), e.getMessage(), e);
            throw e;
        }
    }
}
