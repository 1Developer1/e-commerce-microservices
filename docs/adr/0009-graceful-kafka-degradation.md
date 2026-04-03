# ADR 0009: Kafka Yokken Graceful Degradation

**Tarih:** 2026-04-04
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
Lokal geliştirme ortamında veya Kafka broker'ı geçici olarak erişilemez olduğunda, servisler Kafka'ya bağlanamıyor. Bu durumda:
- product-service'in Kafka consumer'ı sürekli reconnect deniyor (WARN logları)
- order-service'in Kafka producer'ı 60 saniye timeout ile bloklanıyor ve sipariş oluşturma başarısız oluyor

## Karar (Decision)
**Kafka erişilemez olduğunda servisler çalışmaya devam edecek:**

1. **order-service:** Kafka publish işlemi try-catch ile sarıldı. Sipariş DB'ye kaydedildikten sonra event publish başarısız olursa loglanır ama sipariş başarılı döner. Kafka producer timeout'ları kısaltıldı (max.block.ms: 5s, delivery.timeout.ms: 10s).

2. **product-service:** `spring.kafka.listener.missing-topics-fatal: false` ile topic yoksa çökmez. Kafka consumer arka planda reconnect dener ama servis HTTP isteklerine cevap vermeye devam eder.

3. **api-gateway:** Redis rate limiter autoconfiguration devre dışı bırakıldı. Redis yokken de çalışır.

## Sonuçlar (Consequences)
**Pozitif:**
- Servisler Kafka olmadan da ayağa kalkıp HTTP isteklerine cevap verebilir
- Lokal geliştirmede sadece `mvn spring-boot:run` ile servisler başlatılabilir (Docker gerekmez)
- Production'da Kafka geçici kesintisinde sipariş alınmaya devam edilir

**Negatif:**
- Sipariş oluşturuldu ama stok düşmedi senaryosu oluşabilir (eventual consistency ihlali)
- Kaybolan event'ler için Transactional Outbox Pattern ileride eklenmeli

## Uyumluluk (Compliance)
- Event publish hatası loglanmalı (WARN seviyesinde, orderId ile)
- İleride outbox tablosu ile garantili delivery sağlanacak
- Monitoring: Kafka producer failure metric'i izlenmeli
