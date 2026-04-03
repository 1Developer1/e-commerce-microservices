# E-Commerce Microservices

Java 17 ile gelistirilmis, **mikroservis mimarisi**, **Apache Kafka** ile event-driven iletisim, **Spring Cloud Gateway** ile merkezi API yonetimi ve **Hexagonal Architecture** prensiplerini uygulayan bir e-ticaret backend sistemi.

> Bu proje, [modular monolith versiyonunun](https://github.com/1Developer1/e-commerce-app) mikroservis donusumudur.

---

## Mimari Felsefe

Moduler monolitteki Clean Architecture prensipleri korunarak, her modul bagimsiz bir servise donusturulmustur. Temel farklar:

| Konu | Moduler Monolit | Mikroservis (Bu Proje) |
|------|-----------------|------------------------|
| Moduller arasi cagri | Java method (ayni JVM) | REST API (ag uzerinden) |
| Event sistemi | In-memory SimpleEventBus | Apache Kafka |
| Veritabani | Tek PostgreSQL | Her servise ayri DB |
| JWT dogrulama | Her endpoint'te filter | Sadece API Gateway'de |
| Deployment | Tek JAR | Servis basina ayri JAR |

### Sistem Mimarisi

```
                    +------------------+
                    |   API Gateway    |
                    |   (port 8080)    |
                    |  JWT + CORS +    |
                    |  Routing         |
                    +--------+---------+
                             |
              +--------------+--------------+
              |                             |
   +----------+----------+      +----------+----------+
   |  Product Service     |      |  Order Service       |
   |  (port 8081)         |      |  (port 8082)         |
   |  - Urun CRUD         |      |  - Siparis olusturma |
   |  - Stok yonetimi     |      |  - Siparis listeleme |
   |  - Kafka Consumer    |      |  - Kafka Producer    |
   +----------+----------+      +----------+----------+
              |                             |
              |     +---------------+       |
              +---->|  Apache Kafka  |<------+
                    |  (port 9092)   |
                    +---------------+
                    
   +----------------+    +----------------+
   | product_db     |    | order_db       |
   | (PostgreSQL)   |    | (PostgreSQL)   |
   +----------------+    +----------------+
```

---

## Servisler

### 1. API Gateway (port 8080)

Tum dis trafik buradan gecer. JWT dogrulama, CORS ve routing merkezi olarak yonetilir.

| Sorumluluk | Detay |
|------------|-------|
| JWT Dogrulama | `Authorization: Bearer <token>` header'indan token dogrulanir, `X-User-Id` header'i eklenir |
| Routing | `/api/v1/products/**` -> product-service, `/api/v1/orders/**` -> order-service |
| CORS | Frontend (localhost:5173) icin yapilandirilmis |
| Auth Endpoint | `/auth/demo-token` ve `/auth/login` ile JWT token uretimi |

**JWT Akisi:**
```
Client --> Authorization: Bearer <jwt> --> API Gateway
  --> JWT dogrula (HMAC256)
  --> userId cikar (JWT subject)
  --> X-User-Id header ekle
  --> Istegi ilgili servise yonlendir

Servisler: @RequestHeader("X-User-Id") ile userId alir
  --> JWT tekrar dogrulamaz, Gateway'e guvenir
```

### 2. Product Service (port 8081)

Urun katalogu yonetimi ve stok islemleri.

**Public API Endpoint'leri (Gateway uzerinden):**

| Metot | Endpoint | Aciklama | HTTP Kodu |
|-------|----------|----------|-----------|
| `POST` | `/api/v1/products` | Yeni urun olusturur | `201 Created` |
| `GET` | `/api/v1/products?page=0&size=20` | Urunleri sayfalayarak listeler | `200 OK` |
| `PUT` | `/api/v1/products/{id}` | Urunu gunceller | `200 OK` |
| `DELETE` | `/api/v1/products/{id}` | Urunu siler | `204 No Content` |

**Internal API (sadece servisler arasi):**

| Metot | Endpoint | Aciklama |
|-------|----------|----------|
| `GET` | `/internal/api/v1/products/{id}` | Urun dogrulama (order-service kullanir) |

**Kafka Consumer:**
- Topic: `order-placed-events`
- Consumer Group: `product-service-stock-group`
- Islem: Siparis olustugunda stok dusurme

### 3. Order Service (port 8082)

Siparis olusturma ve listeleme. Product-service'e REST ile urun dogrulama yapar, Kafka'ya event publish eder.

**Public API Endpoint'leri (Gateway uzerinden):**

| Metot | Endpoint | Aciklama | HTTP Kodu |
|-------|----------|----------|-----------|
| `POST` | `/api/v1/orders` | Siparis olusturur (items body'de) | `201 Created` |
| `GET` | `/api/v1/orders?page=0&size=20` | Siparisleri listeler | `200 OK` |
| `GET` | `/api/v1/orders/{orderId}` | Siparis detayini getirir | `200 OK` |

**Servisler Arasi Iletisim:**

| Yontem | Hedef | Aciklama |
|--------|-------|----------|
| REST (senkron) | product-service | Siparis olustururken urun dogrulama + fiyat alma |
| Kafka (asenkron) | product-service | Siparis sonrasi stok dusurme event'i |

**Resilience4j:**
- Circuit Breaker: product-service erisilemediyse %50 hata esiginde devre acar
- Retry: 3 deneme, 500ms aralikla

---

## Hexagonal Architecture (Her Serviste)

Monolitteki ayni pattern korunmustur:

```
com.ecommerce.{service}/
+-- entity/                  --> Domain Entity (saf Java, framework bagimliligi yok)
+-- usecase/                 --> Use Case + Port Interface
|   +-- port/                --> Output Port'lar (ProductServicePort, OrderEventPublisher)
+-- adapter/
|   +-- in/
|   |   +-- controller/      --> REST Controller (Inbound Adapter)
|   |   +-- event/           --> Kafka Consumer (Inbound Adapter)
|   |   +-- presenter/       --> Response formatlama
|   +-- out/
|       +-- persistence/     --> JPA Adapter (Outbound)
|       +-- client/          --> REST Client - servisler arasi (Outbound, YENi)
|       +-- event/           --> Kafka Producer (Outbound, YENi)
+-- config/                  --> Spring @Configuration
+-- {Service}Application.java
```

**Monolitten farkli adapter tipleri:**
- `adapter/out/client/` -- Baska servise REST cagrisi yapan adapter (ProductServiceClient)
- `adapter/out/event/` -- Kafka'ya event publish eden adapter (KafkaOrderEventPublisher)

---

## Event-Driven Iletisim (Apache Kafka)

Monolitteki in-memory `SimpleEventBus` yerine Apache Kafka kullanilmaktadir.

### Kafka Akisi

```
order-service: PlaceOrderUseCase
  --> OrderPlacedEvent olustur
  --> KafkaOrderEventPublisher.publishOrderPlaced()
  --> KafkaTemplate.send("order-placed-events", event)
      |
      v (ag uzerinden, asenkron)
  Apache Kafka Broker (port 9092)
      |
      v
product-service: OrderPlacedKafkaConsumer
  --> @KafkaListener(topics = "order-placed-events")
  --> DeductProductStockUseCase.execute(productQuantities)
  --> Stok dusurulur
```

### Kafka Yapilandirmasi

| Parametre | Deger |
|-----------|-------|
| Topic | `order-placed-events` |
| Consumer Group | `product-service-stock-group` |
| Key Serializer | StringSerializer (orderId) |
| Value Serializer | JsonSerializer (OrderPlacedEvent) |
| Trusted Packages | `com.ecommerce.shared.*` |

### Graceful Degradation

Kafka erisilemediyse:
- **order-service:** Siparis DB'ye kaydedilir, event publish basarisiz olursa loglanir ama siparis basarili doner
- **product-service:** Consumer arka planda reconnect dener, HTTP isteklerine cevap vermeye devam eder

---

## Database per Service

Her servisin kendi veritabani vardir:

| Servis | Veritabani | Tablolar |
|--------|------------|----------|
| product-service | `product_db` (port 5433) | `products` |
| order-service | `order_db` (port 5434) | `orders`, `order_items` |

**Lokal gelistirmede:** H2 in-memory (varsayilan)
**Docker'da:** PostgreSQL 15 Alpine (ayri container'lar)

Cross-service SQL JOIN **imkansizdir** -- bu tasarim geregi. Gerekli veri REST API veya Kafka event'leri uzerinden alinir.

---

## Guvenlik

### JWT Authentication

| Bilesen | Konum | Sorumluluk |
|---------|-------|-----------|
| `JwtAuthenticationFilter` | api-gateway | Token dogrulama, userId cikarma, X-User-Id header ekleme |
| `AuthEndpoint` | api-gateway | Demo token uretimi (`/auth/demo-token`, `/auth/login`) |

**Korunan endpoint'ler:** `/api/v1/**` (JWT zorunlu)
**Acik endpoint'ler:** `/auth/**`, `/actuator/**`

### Hata Yonetimi

Her serviste `GlobalExceptionHandler` (`@ControllerAdvice`) ile tutarli hata formati:

```json
{
  "title": "Business Rule Violation",
  "status": 400,
  "detail": "Not enough stock",
  "timestamp": "2026-04-04T10:15:30Z"
}
```

**Guvenlik:** Generic exception mesajlari asla istemciye sizdirilmaz.

---

## Shared Kernel

Tum servislerin ortak kullandigi siniflar `shared-kernel` Maven modulunde:

| Sinif | Amac |
|-------|------|
| `Money` | Parasal deger value object (Jackson annotation'lari ile) |
| `DomainEvent` | Event marker interface |
| `OrderPlacedEvent` | Kafka uzerinden tasinan event DTO |
| `ProductValidationResponse` | Servisler arasi REST response DTO |

`shared-kernel` Spring Boot bagimliligi **icermez** -- sade JAR.

---

## API Endpoint'leri (Tumu)

> Tum istekler API Gateway (port 8080) uzerinden yapilir.
> `Authorization: Bearer <jwt-token>` header'i zorunludur (auth haric).

### Kimlik Dogrulama
| Metot | Endpoint | Aciklama | Yetki |
|-------|----------|----------|-------|
| `POST` | `/auth/login` | userId ile JWT token uretir | Public |
| `POST` | `/auth/demo-token` | Random userId ile demo token | Public |

### Urunler
| Metot | Endpoint | Aciklama | Yetki |
|-------|----------|----------|-------|
| `POST` | `/api/v1/products` | Yeni urun olusturur | JWT |
| `GET` | `/api/v1/products?page=0&size=20` | Urunleri listeler | JWT |
| `PUT` | `/api/v1/products/{id}` | Urunu gunceller | JWT |
| `DELETE` | `/api/v1/products/{id}` | Urunu siler | JWT |

### Siparisler
| Metot | Endpoint | Aciklama | Yetki |
|-------|----------|----------|-------|
| `POST` | `/api/v1/orders` | Siparis olusturur (items body'de) | JWT |
| `GET` | `/api/v1/orders?page=0&size=20` | Siparisleri listeler | JWT |
| `GET` | `/api/v1/orders/{orderId}` | Siparis detayi | JWT |

### Operasyonel
| Metot | Endpoint | Aciklama | Yetki |
|-------|----------|----------|-------|
| `GET` | `/actuator/health` | Servis sagligi | Public |

---

## Tasarim Kaliplari (Design Patterns)

| Kalip | Kullanim Yeri |
|-------|--------------|
| **Hexagonal Architecture** | Tum servislerde: entity -> usecase -> adapter |
| **API Gateway** | Spring Cloud Gateway: merkezi JWT, routing, CORS |
| **Database per Service** | product_db, order_db ayri veritabanlari |
| **Event-Driven** | Kafka ile asenkron stok dusurme |
| **Circuit Breaker** | Resilience4j: order -> product REST cagrisinda |
| **Port/Adapter** | ProductServicePort, OrderEventPublisher interface'leri |
| **Repository** | ProductRepository, OrderRepository port interface'leri |
| **Factory Method** | `Order.create()`, `Product.create()` |
| **Value Object** | `Money` -- immutable, equals/hashCode override |
| **Presenter** | `ProductPresenter`, `OrderPresenter` -- UseCase Output -> ViewModel |
| **Graceful Degradation** | Kafka yokken siparis yine olusturulur |

---

## Mimari Karar Kayitlari (ADR)

| # | Karar | Dosya |
|---|-------|-------|
| 001 | Mikroservis mimarisine gecis | `docs/adr/0001-microservices-adoption.md` |
| 002 | Database per Service pattern | `docs/adr/0002-database-per-service.md` |
| 003 | Apache Kafka ile event-driven iletisim | `docs/adr/0003-kafka-event-driven-communication.md` |
| 004 | API Gateway pattern (Spring Cloud Gateway) | `docs/adr/0004-api-gateway-pattern.md` |
| 005 | Shared Kernel kutuphanesi | `docs/adr/0005-shared-kernel-library.md` |
| 006 | Servisler arasi iletisim stratejisi (REST + Kafka) | `docs/adr/0006-inter-service-communication.md` |
| 007 | Hexagonal architecture korunmasi | `docs/adr/0007-hexagonal-architecture-preserved.md` |
| 008 | Cart eliminasyonu, dogrudan siparis item'lari | `docs/adr/0008-cart-elimination-direct-order-items.md` |
| 009 | Kafka yokken graceful degradation | `docs/adr/0009-graceful-kafka-degradation.md` |
| 010 | Merkezi hata yonetimi stratejisi | `docs/adr/0010-error-handling-strategy.md` |

---

## Quickstart

### Gereksinimler
- Java 17+
- Maven 3.8+
- Docker & Docker Compose (opsiyonel, tam altyapi icin)

### Lokal Calistirma (H2, Kafka opsiyonel)

```bash
# 1. Repoyu klonla
git clone https://github.com/1Developer1/e-commerce-microservices.git
cd e-commerce-microservices

# 2. Derle
mvn clean package -DskipTests

# 3. Servisleri baslat (ayri terminallerde)
java -jar product-service/target/product-service-1.0-SNAPSHOT.jar
java -jar order-service/target/order-service-1.0-SNAPSHOT.jar
java -jar api-gateway/target/api-gateway-1.0-SNAPSHOT.jar
```

### Docker Compose ile Calistirma (Tam Altyapi)

```bash
docker-compose up --build
```

Bu komut su container'lari baslatir:
- `ms-product-db` (PostgreSQL, port 5433)
- `ms-order-db` (PostgreSQL, port 5434)
- `ms-zookeeper` (port 2181)
- `ms-kafka` (port 9092, 29092)
- `ms-redis` (port 6379)
- `ms-product-service` (port 8081)
- `ms-order-service` (port 8082)
- `ms-api-gateway` (port 8080)

### Token Alma ve API Kullanimi

```bash
# 1. Demo JWT token al
TOKEN=$(curl -s -X POST http://localhost:8080/auth/demo-token \
  | python -m json.tool | grep token | cut -d'"' -f4)

# 2. Urunleri listele
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/products

# 3. Siparis olustur
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "recipientName": "Ahmet Yilmaz",
    "shippingAddress": "Kadikoy, Istanbul",
    "items": [
      {"productId": "11111111-1111-1111-1111-111111111111", "quantity": 1},
      {"productId": "22222222-2222-2222-2222-222222222222", "quantity": 2}
    ]
  }'

# 4. Siparisleri listele
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/orders

# 5. JWT olmadan erisim (401 olmali)
curl -s -o /dev/null -w "Status: %{http_code}" http://localhost:8080/api/v1/products
```

---

## Proje Yapisi

```
e-commerce-microservices/
+-- pom.xml                          (Parent POM - multi-module Maven)
+-- docker-compose.yml               (8 container: 2 DB + Kafka + Zookeeper + Redis + 3 servis)
+-- README.md
+-- docs/adr/                        (10 Mimari Karar Kaydi)
+-- shared-kernel/                   (Ortak JAR: Money, DomainEvent, DTO)
+-- product-service/                 (Port 8081)
|   +-- Dockerfile
|   +-- src/main/java/.../product/
|   |   +-- entity/Product.java
|   |   +-- usecase/                 (6 use case + ProductRepository port)
|   |   +-- adapter/in/controller/   (ProductController + InternalProductController)
|   |   +-- adapter/in/event/        (OrderPlacedKafkaConsumer)
|   |   +-- adapter/out/persistence/ (JPA adapter)
|   |   +-- config/                  (UseCaseConfig, GlobalExceptionHandler)
|   +-- src/main/resources/
|       +-- application.yml
|       +-- db/migration/V1__product_schema.sql
+-- order-service/                   (Port 8082)
|   +-- Dockerfile
|   +-- src/main/java/.../order/
|   |   +-- entity/Order.java, OrderItem.java
|   |   +-- usecase/                 (3 use case + port/ dizini)
|   |   +-- adapter/in/controller/   (OrderController)
|   |   +-- adapter/out/persistence/ (JPA adapter)
|   |   +-- adapter/out/client/      (ProductServiceClient + Resilience4j)
|   |   +-- adapter/out/event/       (KafkaOrderEventPublisher)
|   |   +-- config/                  (UseCaseConfig, GlobalExceptionHandler)
|   +-- src/main/resources/
|       +-- application.yml
|       +-- db/migration/V1__order_schema.sql
+-- api-gateway/                     (Port 8080)
    +-- Dockerfile
    +-- src/main/java/.../gateway/
    |   +-- filter/JwtAuthenticationFilter.java
    |   +-- config/CorsConfig.java, AuthEndpoint.java
    +-- src/main/resources/
        +-- application.yml
```

---

## Teknoloji Stack

| Katman | Teknoloji |
|--------|----------|
| **Dil** | Java 17 |
| **Build** | Maven (Multi-module) |
| **Web Framework** | Spring Boot 3.2.4 |
| **API Gateway** | Spring Cloud Gateway 2023.0.1 |
| **Persistence** | Spring Data JPA + Flyway + H2 / PostgreSQL |
| **Messaging** | Apache Kafka (Confluent 7.5.0) |
| **Resilience** | Resilience4j (CircuitBreaker, Retry) |
| **Security** | JWT (Auth0 java-jwt 4.4.0) |
| **Containerization** | Docker (Multi-stage Alpine) + Docker Compose |
| **Veritabani** | PostgreSQL 15 Alpine (production) / H2 (development) |

---

## Port Ozeti

| Servis | Port (Lokal) | Port (Docker) |
|--------|-------------|---------------|
| API Gateway | 8080 | 8080 |
| Product Service | 8081 | 8081 |
| Order Service | 8082 | 8082 |
| PostgreSQL (product) | - | 5433 -> 5432 |
| PostgreSQL (order) | - | 5434 -> 5432 |
| Kafka (Docker ici) | - | 9092 |
| Kafka (Host erisim) | 29092 | 29092 |
| Redis | 6379 | 6379 |
| Zookeeper | 2181 | 2181 |
