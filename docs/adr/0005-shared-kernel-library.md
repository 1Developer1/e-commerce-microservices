# ADR 0005: Shared Kernel Kütüphanesi

**Tarih:** 2026-04-03
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
Monolitte `com.ecommerce.shared` paketi tüm modüllerin kullandığı ortak sınıfları içeriyordu: `Money` value object, `DomainEvent` interface, `EventBus` interface, `SimpleEventBus` implementasyonu.

Mikroserviste her servis ayrı bir JVM. Ortak sınıflar (Money, event DTO'ları) her serviste tekrar mı yazılacak, yoksa paylaşımlı bir kütüphane mi olacak?

Değerlendirilen Seçenekler:
1. Her serviste kopyala-yapıştır (bağımsız, ama code duplication)
2. Shared JAR kütüphanesi (Maven dependency olarak)
3. Protobuf/Avro schema'lar ile kontrat paylaşımı

## Karar (Decision)
**shared-kernel** adında ayrı bir Maven modülü oluşturulacak. Tüm servisler bu JAR'ı dependency olarak kullanacak.

İçerik:
- `com.ecommerce.shared.domain.Money` — Parasal değer value object (Jackson annotation'ları ile)
- `com.ecommerce.shared.event.DomainEvent` — Event marker interface
- `com.ecommerce.shared.event.OrderPlacedEvent` — Kafka üzerinden taşınan event DTO
- `com.ecommerce.shared.dto.ProductValidationResponse` — Servisler arası REST response DTO

İçermeyecekler:
- `SimpleEventBus` → her servis kendi Kafka implementasyonunu kullanacak
- `EventBus` interface → artık Kafka doğrudan kullanılacak (Spring Kafka)
- İş mantığı → paylaşımlı kütüphanede iş kuralı olmamalı

## Sonuçlar (Consequences)
**Pozitif:**
- Tip güvenliği: Producer ve consumer aynı OrderPlacedEvent sınıfını kullanır → deserialization hatası olmaz
- DRY: Money sınıfı tek yerde tanımlı, tüm servisler aynı versiyonu kullanır
- Kafka uyumu: Jackson annotation'ları (@JsonCreator, @JsonProperty) shared-kernel'de tanımlı

**Negatif:**
- Versiyon bağımlılığı: shared-kernel değiştiğinde tüm servisler yeniden build edilmeli
- Tight coupling riski: Çok şey shared-kernel'e eklenirse servisler bağımsızlığını kaybeder
- Breaking change riski: Event DTO'sundaki bir değişiklik tüm consumer'ları etkiler

## Uyumluluk (Compliance)
- shared-kernel'de Spring Boot bağımlılığı OLMAYACAK (sade JAR)
- Sadece value object, DTO ve interface içerecek — asla iş mantığı içermeyecek
- Event DTO'ları geriye uyumlu tutulacak (alan ekle, silme; alan adı değiştirme)
- Semantic versioning ile yönetilecek (major: breaking, minor: yeni alan, patch: düzeltme)
