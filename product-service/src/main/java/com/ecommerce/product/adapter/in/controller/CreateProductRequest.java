package com.ecommerce.product.adapter.in.controller;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 255) String name,

        @Size(max = 2000) String description,

        @NotNull @DecimalMin("0.01") BigDecimal priceAmount,
        @NotBlank @Size(max = 3) String priceCurrency,
        @Min(0) int initialStock
) {}
