package com.ecommerce.shared.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record OrderPlacedEvent(
        @JsonProperty("orderId") UUID orderId,
        @JsonProperty("productQuantities") Map<UUID, Integer> productQuantities,
        @JsonProperty("occurredOn") LocalDateTime occurredOn
) implements DomainEvent {

    @JsonCreator
    public OrderPlacedEvent {}
}
