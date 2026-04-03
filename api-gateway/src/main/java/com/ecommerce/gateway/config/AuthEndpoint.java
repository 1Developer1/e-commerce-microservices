package com.ecommerce.gateway.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class AuthEndpoint {

    @Value("${jwt.secret:changeme-in-production}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600000}")
    private long jwtExpiration;

    @Bean
    public RouterFunction<ServerResponse> authRoutes() {
        return RouterFunctions
                .route(RequestPredicates.POST("/auth/login"), this::login)
                .andRoute(RequestPredicates.POST("/auth/demo-token"), this::demoToken);
    }

    private Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(Map.class).flatMap(body -> {
            // Demo: accept any userId, generate token
            String userIdStr = (String) body.get("userId");
            if (userIdStr == null || userIdStr.isBlank()) {
                return ServerResponse.badRequest().bodyValue(Map.of("error", "userId is required"));
            }

            UUID userId;
            try {
                userId = UUID.fromString(userIdStr);
            } catch (IllegalArgumentException e) {
                return ServerResponse.badRequest().bodyValue(Map.of("error", "Invalid userId format"));
            }

            String token = generateToken(userId);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("userId", userId.toString());
            response.put("message", "Demo login successful");
            return ServerResponse.ok().bodyValue(response);
        });
    }

    private Mono<ServerResponse> demoToken(ServerRequest request) {
        UUID userId = UUID.randomUUID();
        String token = generateToken(userId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("token", token);
        response.put("userId", userId.toString());
        response.put("message", "Demo token generated with random userId");
        return ServerResponse.ok().bodyValue(response);
    }

    private String generateToken(UUID userId) {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        return JWT.create()
                .withSubject(userId.toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpiration))
                .sign(algorithm);
    }
}
