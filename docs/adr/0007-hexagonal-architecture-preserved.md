# ADR 0007: Hexagonal Architecture — Mikroservislerde Korunması

**Tarih:** 2026-04-03
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
Monolitte her modül hexagonal architecture ile yapılandırılmıştı:
- `entity/` — Domain entity'ler (saf Java, framework bağımlılığı yok)
- `usecase/` — İş mantığı + port interface'leri
- `adapter/in/controller/` — HTTP giriş noktası
- `adapter/in/event/` — Event listener giriş noktası
- `adapter/out/persistence/` — JPA implementasyonu
- `adapter/in/presenter/` — Response formatlama

Mikroservise geçişte bu yapı korunacak mı, yoksa her servis daha basit bir yapıya mı geçecek?

## Karar (Decision)
**Hexagonal architecture korunacak.** Her mikroserviste aynı paket yapısı:

```
com.ecommerce.{service}/
├── entity/              → Domain entity (saf Java)
├── usecase/             → Use case + port interface
│   └── port/            → Output port'lar (ProductServicePort, OrderEventPublisher)
├── adapter/
│   ├── in/
│   │   ├── controller/  → REST controller
│   │   ├── event/       → Kafka consumer (monolitteki EventHandler karşılığı)
│   │   └── presenter/   → Response formatlama
│   └── out/
│       ├── persistence/  → JPA adapter
│       ├── client/       → REST client (servisler arası, YENİ)
│       └── event/        → Kafka producer (YENİ)
├── config/              → Spring @Configuration
└── {Service}Application.java
```

Monolitten fark: `adapter/out/` altına iki yeni adapter tipi eklendi:
- `client/` — Başka servise REST çağrı yapan adapter (ProductServiceClient)
- `event/` — Kafka'ya event publish eden adapter (KafkaOrderEventPublisher)

## Sonuçlar (Consequences)
**Pozitif:**
- Tutarlılık: Monolitle aynı mimari dil, yeni geliştirici geçişi kolay
- Test edilebilirlik: Use case, adapter'dan bağımsız test edilebilir (port mock)
- Teknoloji değişimi: Kafka yerine RabbitMQ → sadece `adapter/out/event/` değişir
- REST client değişimi: RestClient yerine Feign → sadece `adapter/out/client/` değişir

**Negatif:**
- Mikroserviste her servis zaten küçük — hexagonal overhead oransal olarak daha büyük
- Basit bir CRUD servisi için bile interface + adapter + use case yazılıyor

## Uyumluluk (Compliance)
- Entity sınıfları hiçbir framework annotation'ı içermeyecek (@Entity, @Component yok)
- Use case sınıfları Spring bağımlılığı taşımayacak (port interface üzerinden erişim)
- Controller, doğrudan repository çağırmayacak — mutlaka use case üzerinden geçecek
- Yeni adapter tipi eklendiğinde (ör: gRPC client) sadece `adapter/out/` altına eklenir
