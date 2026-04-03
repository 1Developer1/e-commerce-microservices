# ADR 0001: Modüler Monolitten Mikroservis Mimarisine Geçiş

**Tarih:** 2026-04-03
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
Mevcut e-ticaret projesi modüler monolit mimarisinde, hexagonal/clean architecture prensipleriyle yazılmış durumda. 8 modül (Product, Cart, Order, Payment, Shipping, Discount, User, Shared) tek bir JVM içinde çalışıyor. Proje büyüdükçe bağımsız deployment, bağımsız ölçekleme ve takım bazlı geliştirme ihtiyaçları ortaya çıktı.

Değerlendirilen Seçenekler:
1. Modüler monoliti korumak (mevcut durum)
2. Tam mikroservis geçişi (tüm modüller ayrı servis)
3. Kademeli geçiş — öncelikli 2-3 servis + API Gateway

## Karar (Decision)
**Kademeli geçiş** stratejisi benimsenecek. İlk aşamada 3 servis oluşturulacak:
- **product-service** (port 8081) — Ürün kataloğu, stok yönetimi
- **order-service** (port 8082) — Sipariş oluşturma ve listeleme
- **api-gateway** (port 8080) — Merkezi JWT doğrulama, routing, CORS, rate limiting

Mevcut monolitin hexagonal yapısı büyük avantaj sağlıyor: port/adapter deseni, facade interface'ler ve domain event altyapısı zaten mevcut.

## Sonuçlar (Consequences)
**Pozitif:**
- Bağımsız deployment: Product servisini Order'dan bağımsız deploy edebilme
- Bağımsız ölçekleme: Sipariş yoğun dönemde sadece order-service scale edilebilir
- Teknoloji çeşitliliği: Her servis farklı DB, farklı framework kullanabilir
- Hata izolasyonu: Product servisi çökse bile mevcut siparişler listelenebilir

**Negatif:**
- Operasyonel karmaşıklık: 3 container → 8+ container
- Ağ gecikmesi: Java method çağrısı (ns) → REST/Kafka (ms)
- Eventual consistency: Strong consistency kaybı, Saga pattern gereksinimi
- Debug zorluğu: Dağıtık trace, centralized logging gereksinimi

## Uyumluluk (Compliance)
- Her servis kendi Flyway migration'larını yönetecek
- Servisler arası iletişim sadece REST veya Kafka üzerinden olacak
- Doğrudan DB erişimi yasak (database-per-service pattern)
