# ADR 0004: API Gateway Pattern — Spring Cloud Gateway

**Tarih:** 2026-04-03
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
Monolitte tüm cross-cutting concern'ler (JWT doğrulama, rate limiting, CORS, audit logging) tek uygulamanın filter/interceptor zincirinde hallediliyordu:
- `JwtAuthenticationFilter` → her istekte token doğrulama
- `RateLimitingFilter` → IP bazlı rate limiting (Resilience4j)
- `SecurityConfig` → CORS ayarları
- `AuditLoggingInterceptor` → mutating request log

Mikroserviste her servis kendi güvenliğini yönetirse: JWT secret paylaşımı, tutarsız CORS ayarları, tekrar eden kod sorunları oluşur.

Değerlendirilen Seçenekler:
1. Spring Cloud Gateway (reactive, WebFlux tabanlı)
2. Kong Gateway (Lua tabanlı, plugin sistemi)
3. NGINX + custom Lua scripts
4. Her serviste kendi güvenlik katmanı (gateway yok)

## Karar (Decision)
**Spring Cloud Gateway** kullanılacak.

Seçim nedenleri:
- Spring ekosistemi ile doğal entegrasyon (mevcut ekip Spring biliyor)
- Reactive (WebFlux) — yüksek eşzamanlılık, non-blocking I/O
- Monolitteki `JwtAuthenticationFilter` mantığı büyük ölçüde yeniden kullanılabilir
- Route bazlı filtreleme (product-service ve order-service'e farklı kurallar)

JWT akışı:
```
Client → Authorization: Bearer <token> → API Gateway
  → JWT doğrula (com.auth0 kütüphanesi, HMAC256)
  → userId çıkar (JWT subject)
  → X-User-Id header ekle
  → İsteği ilgili servise yönlendir

Servisler: @RequestHeader("X-User-Id") ile userId alır
  → JWT tekrar doğrulamaz, Gateway'e güvenir
```

Route yapısı:
- `/api/v1/products/**` → http://product-service:8081
- `/api/v1/orders/**` → http://order-service:8082
- `/auth/**` → Gateway içinde (demo token üretimi)
- `/actuator/**` → public (health check)

## Sonuçlar (Consequences)
**Pozitif:**
- Tek güvenlik noktası: JWT doğrulama, rate limiting, CORS tek yerde
- Servisler basitleşiyor: Güvenlik concern'i yok, sadece X-User-Id header okuma
- Frontend değişikliği yok: Aynı port (8080), aynı URL yapısı
- Merkezi monitoring: Tüm trafik gateway'den geçiyor

**Negatif:**
- Single point of failure: Gateway down → tüm sistem erişilemez (HA ile çözülür)
- Ek latency: Her istek gateway üzerinden geçiyor (~1-2ms ek gecikme)
- Reactive paradigma: WebFlux, geleneksel Spring MVC'den farklı (Mono/Flux)

## Uyumluluk (Compliance)
- Servisler asla doğrudan dış dünyaya açılmayacak (sadece Gateway erişilebilir)
- `/internal/**` endpoint'leri Gateway route'larına eklenmeyecek (servisler arası özel)
- JWT secret Gateway ve auth-service arasında paylaşılacak (environment variable)
- Rate limit aşıldığında RFC 7807 formatında hata dönülecek
