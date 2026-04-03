package com.ecommerce.product.adapter.in.controller;

import com.ecommerce.product.usecase.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final CreateProductUseCase createUseCase;
    private final ListProductsUseCase listUseCase;
    private final UpdateProductUseCase updateUseCase;
    private final DeleteProductUseCase deleteUseCase;

    public ProductController(CreateProductUseCase createUseCase, ListProductsUseCase listUseCase,
                             UpdateProductUseCase updateUseCase, DeleteProductUseCase deleteUseCase) {
        this.createUseCase = createUseCase;
        this.listUseCase = listUseCase;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@Valid @RequestBody CreateProductRequest req) {
        var output = createUseCase.execute(new CreateProductUseCase.Input(
                req.name(), req.description(), req.priceAmount(), req.priceCurrency(), req.initialStock()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", output.success());
        body.put("message", output.message());
        body.put("productId", output.id().toString());
        body.put("name", output.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var output = listUseCase.execute(page, size);
        int totalPages = output.size() > 0 ? (int) Math.ceil((double) output.totalElements() / output.size()) : 0;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("products", output.products());
        body.put("page", output.page());
        body.put("size", output.size());
        body.put("totalElements", output.totalElements());
        body.put("totalPages", totalPages);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(output.totalElements()))
                .header("X-Total-Pages", String.valueOf(totalPages))
                .body(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable UUID id, @Valid @RequestBody UpdateProductRequest req) {
        var output = updateUseCase.execute(id, req.name(), req.description(),
                req.priceAmount(), req.priceCurrency(), req.stockQuantity());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", output.success());
        body.put("message", output.message());
        if (!output.success()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        if (!deleteUseCase.execute(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
