package com.ecommerce.product.config;

import com.ecommerce.product.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreateProductUseCase createProductUseCase(ProductRepository repo) {
        return new CreateProductUseCase(repo);
    }

    @Bean
    public ListProductsUseCase listProductsUseCase(ProductRepository repo) {
        return new ListProductsUseCase(repo);
    }

    @Bean
    public GetProductByIdUseCase getProductByIdUseCase(ProductRepository repo) {
        return new GetProductByIdUseCase(repo);
    }

    @Bean
    public UpdateProductUseCase updateProductUseCase(ProductRepository repo) {
        return new UpdateProductUseCase(repo);
    }

    @Bean
    public DeleteProductUseCase deleteProductUseCase(ProductRepository repo) {
        return new DeleteProductUseCase(repo);
    }

    @Bean
    public DeductProductStockUseCase deductProductStockUseCase(ProductRepository repo) {
        return new DeductProductStockUseCase(repo);
    }
}
