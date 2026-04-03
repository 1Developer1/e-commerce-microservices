package com.ecommerce.product.adapter.in.controller;

import java.math.BigDecimal;

public record UpdateProductRequest(
        String name,
        String description,
        BigDecimal priceAmount,
        String priceCurrency,
        Integer stockQuantity
) {}
