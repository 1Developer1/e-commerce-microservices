package com.ecommerce.product.adapter.in.controller;

import com.ecommerce.product.usecase.GetProductByIdUseCase;
import com.ecommerce.shared.dto.ProductValidationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/internal/api/v1/products")
public class InternalProductController {

    private final GetProductByIdUseCase getProductByIdUseCase;

    public InternalProductController(GetProductByIdUseCase getProductByIdUseCase) {
        this.getProductByIdUseCase = getProductByIdUseCase;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductValidationResponse> getProduct(@PathVariable UUID id) {
        ProductValidationResponse response = getProductByIdUseCase.execute(id);
        if (!response.exists()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(response);
    }
}
