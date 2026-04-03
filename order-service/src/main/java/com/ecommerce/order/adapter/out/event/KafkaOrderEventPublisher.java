package com.ecommerce.order.adapter.out.event;

import com.ecommerce.order.usecase.port.OrderEventPublisher;
import com.ecommerce.shared.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaOrderEventPublisher implements OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaOrderEventPublisher.class);
    private static final String TOPIC = "order-placed-events";

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public KafkaOrderEventPublisher(KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishOrderPlaced(OrderPlacedEvent event) {
        log.info("[Kafka Producer] Publishing OrderPlacedEvent for orderId={}", event.orderId());
        try {
            kafkaTemplate.send(TOPIC, event.orderId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("[Kafka Producer] Failed to publish event for orderId={}: {}",
                                    event.orderId(), ex.getMessage());
                        } else {
                            log.info("[Kafka Producer] Event published successfully for orderId={}",
                                    event.orderId());
                        }
                    });
        } catch (Exception e) {
            log.error("[Kafka Producer] Kafka unavailable, event not published for orderId={}: {}",
                    event.orderId(), e.getMessage());
        }
    }
}
