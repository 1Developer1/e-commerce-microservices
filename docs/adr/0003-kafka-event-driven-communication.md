# ADR 0003: Apache Kafka ile Event-Driven İletişim

**Tarih:** 2026-04-03
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
Monolitte modüller arası asenkron iletişim `SimpleEventBus` (in-memory, ConcurrentHashMap tabanlı) ile sağlanıyordu. Bu çözüm aynı JVM içinde çalışıyor, aslında senkron, ve uygulama crash olursa event'ler kayboluyordu.

Mikroserviste servisler farklı JVM'lerde çalıştığı için in-memory event bus kullanılamaz. Servisler arası asenkron mesajlaşma için bir message broker gerekiyor.

Değerlendirilen Seçenekler:
1. Apache Kafka — log-based, high throughput, event replay
2. RabbitMQ — traditional message queue, simpler ops
3. Redis Streams — lightweight, already using Redis for rate limiting

## Karar (Decision)
**Apache Kafka** (Confluent CP 7.5.0, Zookeeper ile) kullanılacak.

Seçim nedenleri:
- Monolitte zaten `outbox_events` tablosu hazır — ileride Debezium CDC ile doğal entegrasyon
- Event replay yeteneği: Yeni consumer eklendiğinde geçmiş event'leri tekrar işleyebilme
- Partition bazlı ordering: Aynı orderId'ye ait event'ler sıralı işlenir
- Yüksek throughput: E-ticaret senaryolarında kampanya dönemlerinde trafik patlaması

Topic yapısı:
- `order-placed-events` — Producer: order-service, Consumer: product-service (stok düşürme)

Serialization:
- Key: StringSerializer (orderId)
- Value: JsonSerializer (OrderPlacedEvent record)

Consumer Group:
- `product-service-stock-group` — product-service instance'ları arasında partitioned consumption

## Sonuçlar (Consequences)
**Pozitif:**
- Gerçek asenkron: Producer mesajı bırakır, consumer'ın işlemesini beklemez
- Mesaj kaybı yok: Kafka disk'e yazar, servis down olsa bile mesaj bekler
- Replay: Consumer offset sıfırlanarak geçmiş event'ler tekrar işlenebilir
- Decouplling: order-service, product-service'in varlığını bilmek zorunda değil

**Negatif:**
- Operasyonel karmaşıklık: Kafka + Zookeeper yönetimi (production'da Strimzi operator önerilir)
- Eventual consistency: Sipariş oluşturuldu ama stok henüz düşmemiş olabilir (ms seviyesinde gecikme)
- Debugging zorluğu: Event akışını izlemek için Kafka UI aracı gerekir
- Idempotency gereksinimi: Aynı event birden fazla kez işlenebilir, consumer idempotent olmalı

## Uyumluluk (Compliance)
- Tüm event DTO'ları shared-kernel modülünde tanımlanacak (producer ve consumer aynı sınıfı kullanacak)
- Consumer'lar idempotent olmalı (aynı event'i tekrar işlerse yan etki olmamalı)
- Topic isimlendirme: `{aggregate}-{action}-events` formatı (ör: `order-placed-events`)
- `spring.json.trusted.packages: com.ecommerce.shared.*` ile güvenli deserialization
