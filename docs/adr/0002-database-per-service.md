# ADR 0002: Database per Service Pattern

**Tarih:** 2026-04-03
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
Monolitte tüm modüller tek PostgreSQL veritabanını paylaşıyordu. products, orders, order_items, carts, cart_items, discounts, users tabloları aynı DB'de, cross-table JOIN ve FK constraint'ler kullanılıyordu.

Mikroservis mimarisinde veritabanı paylaşımı servislerin bağımsızlığını bozar: bir servisin schema değişikliği diğerini etkiler, bir servisin DB yükü diğerini yavaşlatır.

Değerlendirilen Seçenekler:
1. Tek paylaşımlı veritabanı (monolitteki gibi)
2. Aynı DB instance, farklı schema'lar
3. Her servise ayrı DB instance

## Karar (Decision)
**Her servise ayrı PostgreSQL veritabanı** kullanılacak:
- `product_db` — product-service (port 5433)
- `order_db` — order-service (port 5434)

Docker Compose'da ayrı PostgreSQL container'lar ile sağlanıyor. Production'da ayrı RDS instance'ları veya aynı cluster'da ayrı database'ler kullanılabilir.

## Sonuçlar (Consequences)
**Pozitif:**
- Tam izolasyon: Bir servisin DB yükü diğerini etkilemez
- Bağımsız migration: Her servis kendi Flyway dosyalarını yönetir
- Teknoloji özgürlüğü: Bir servis PostgreSQL, diğeri MongoDB kullanabilir

**Negatif:**
- Cross-service JOIN imkansız: `SELECT o.*, p.name FROM orders o JOIN products p` artık yapılamaz
- Veri tekrarı (denormalizasyon): order_items tablosunda product_name saklanıyor çünkü product-service'e JOIN yapılamaz
- Distributed transaction: Tek @Transactional yetmez, Saga pattern gerekir
- Referential integrity kaybı: Servisler arası FK constraint yok

## Uyumluluk (Compliance)
- Servisler birbirinin DB'sine doğrudan bağlanmayacak
- Gerekli veri REST API veya Kafka event'leri üzerinden alınacak
- Denormalize edilen veriler (product_name gibi) event'ler ile güncel tutulacak
