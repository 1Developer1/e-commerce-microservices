package com.ecommerce.shared.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductValidationResponse(
        @JsonProperty("id") UUID id,
        @JsonProperty("name") String name,
        @JsonProperty("priceAmount") BigDecimal priceAmount,
        @JsonProperty("priceCurrency") String priceCurrency,
        @JsonProperty("stockQuantity") int stockQuantity,
        @JsonProperty("exists") boolean exists
) {
    @JsonCreator
    public ProductValidationResponse {}

    public static ProductValidationResponse notFound(UUID id) {
        return new ProductValidationResponse(id, null, null, null, 0, false);
    }
}
