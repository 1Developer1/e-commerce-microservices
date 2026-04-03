# ADR 0010: Merkezi Hata Yonetimi Stratejisi

**Tarih:** 2026-04-04
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
Mikroservislerde hata yönetimi monolitten farklı çalışıyor:
- Her servisin kendi hata tipleri var (ürün bulunamadı vs sipariş bulunamadı)
- Servisler arası REST çağrılarda hata propagation'ı gerekiyor
- İç detaylar (stack trace, DB hataları) asla client'a sızdırılmamalı

Monolitte tek bir GlobalExceptionHandler tüm hataları yönetiyordu. Mikroserviste her servisin kendi handler'ı olmalı.

## Karar (Decision)
Her serviste ayrı `GlobalExceptionHandler` (`@ControllerAdvice`) oluşturulacak. Tüm handler'lar aynı response formatını kullanacak:

```json
{
  "title": "Business Rule Violation",
  "status": 400,
  "detail": "Not enough stock",
  "timestamp": "2026-04-04T10:15:30Z"
}
```

Handler'lar üç katman halinde hata yakalar:
1. **Domain hataları** (IllegalArgumentException, IllegalStateException) → 400
2. **Validasyon hataları** (MethodArgumentNotValidException) → 400 + violations listesi
3. **Beklenmeyen hatalar** (Exception) → 500 + genel mesaj (iç detay sızdırılmaz)

Servisler arası REST çağrılarda (order → product):
- Circuit breaker fallback ile hata yakalanır
- ProductServiceClient fallback'i `ProductValidationResponse.notFound()` döner
- Use case bu response'u kontrol eder ve uygun hata mesajı döner

## Sonuçlar (Consequences)
**Pozitif:**
- Tutarlı hata formatı tüm servislerde
- İç detaylar asla sızdırılmaz (güvenlik)
- Servisler arası hata propagation'ı circuit breaker fallback ile yönetilir

**Negatif:**
- Her serviste aynı exception handler kodu tekrar ediyor (shared-kernel'e taşınabilir ama Spring bağımlılığı olur)

## Uyumluluk (Compliance)
- Generic Exception handler'da `ex.getMessage()` asla client'a dönmeyecek
- Tüm hatalar `title + status + detail + timestamp` formatında olacak
- Production'da hata logları structured JSON formatında olacak (ELK uyumlu)
