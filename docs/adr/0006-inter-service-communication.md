# ADR 0006: Servisler Arası İletişim Stratejisi (REST + Kafka)

**Tarih:** 2026-04-03
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
Monolitte modüller arası iletişim iki şekilde yapılıyordu:
1. **Senkron:** Java method çağrısı (`cartService.getCartForOrder()`, `orderQueryPort.findOrderTotal()`)
2. **Asenkron:** In-memory EventBus (`OrderPlacedEvent → OrderPlacedStockHandler`)

Mikroserviste Java method çağrısı yapılamaz — servisler farklı JVM'lerde. İki iletişim modeli gerekiyor.

## Karar (Decision)
**İki iletişim modeli paralel kullanılacak:**

### Senkron (REST) — "Cevap lazım, bekleyeceğim"
- **order-service → product-service:** Sipariş oluştururken ürün doğrulama
- Endpoint: `GET /internal/api/v1/products/{id}`
- Resilience4j Circuit Breaker + Retry ile sarmalanmış
- `ProductServicePort` (interface) → `ProductServiceClient` (implementasyon)
- Spring RestClient kullanılacak

### Asenkron (Kafka) — "Haber veriyorum, beklemiyorum"
- **order-service → product-service:** Sipariş sonrası stok düşürme
- Topic: `order-placed-events`
- `OrderEventPublisher` (interface) → `KafkaOrderEventPublisher` (implementasyon)

### Karar Kuralı
```
Bu veriye ŞİMDİ ihtiyacım var mı? → REST (senkron)
Sadece haber veriyorum, cevap gerekmiyor → Kafka (asenkron)
```

Somut örnekler:
| İletişim | Yöntem | Neden |
|----------|--------|-------|
| Order → Product: "Bu ürün var mı, fiyatı ne?" | REST | Fiyat bilgisi olmadan sipariş oluşturulamaz |
| Order → Product: "Stok düşür" | Kafka | Stok düşürme sonucunu beklemeye gerek yok |

## Sonuçlar (Consequences)
**Pozitif:**
- Hexagonal architecture korunuyor: Port interface → Adapter implementasyonu
- Resilience: Circuit breaker ile product-service down olduğunda order-service çökmez
- Decoupling: Kafka ile order-service, product-service'in varlığını bilmek zorunda değil

**Negatif:**
- İki farklı iletişim modeli = iki farklı hata yönetimi stratejisi
- REST: timeout, connection refused, 5xx hatalar → circuit breaker fallback
- Kafka: deserialization hatası, consumer lag → dead letter topic

## Uyumluluk (Compliance)
- Senkron çağrılarda Resilience4j circuit breaker ZORUNLU
- Asenkron event'lerde consumer idempotency ZORUNLU
- `/internal/**` endpoint'leri sadece servisler arası erişim için (Gateway'den erişilemez)
- Port/Adapter pattern korunacak: use case doğrudan RestClient veya KafkaTemplate çağırmayacak
