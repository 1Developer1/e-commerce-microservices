package com.ecommerce.order.adapter.out.client;

import com.ecommerce.order.usecase.port.ProductServicePort;
import com.ecommerce.shared.dto.ProductValidationResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class ProductServiceClient implements ProductServicePort {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceClient.class);
    private final RestClient restClient;

    public ProductServiceClient(@Value("${product-service.url:http://localhost:8081}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    @Retry(name = "productService")
    public ProductValidationResponse getProduct(UUID productId) {
        log.debug("Fetching product {} from product-service", productId);
        return restClient.get()
                .uri("/internal/api/v1/products/{id}", productId)
                .retrieve()
                .body(ProductValidationResponse.class);
    }

    private ProductValidationResponse getProductFallback(UUID productId, Throwable t) {
        log.error("Product service unavailable for product {}: {}", productId, t.getMessage());
        return ProductValidationResponse.notFound(productId);
    }
}
