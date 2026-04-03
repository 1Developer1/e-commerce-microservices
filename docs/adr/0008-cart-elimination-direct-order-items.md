# ADR 0008: Cart Servisi Yerine Doğrudan Sipariş Item'ları

**Tarih:** 2026-04-03
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
Monolitte sipariş akışı şöyleydi:
1. `PlaceOrderUseCase` → `cartService.getCartForOrder(userId)` (sepeti al)
2. Sepetteki item'lardan Order oluştur
3. `cartService.clearCart(userId)` (sepeti temizle)
4. `eventBus.publish(OrderPlacedEvent)` (stok düşür)

Bu akış Order modülünün Cart modülüne senkron bağımlılığını gerektiriyordu. Mikroserviste bu, Order Service'in Cart Service'e REST çağrı yapması ve Saga pattern ile tutarlılık sağlaması anlamına gelir.

İlk aşamada (Phase 1) sadece Product ve Order servisleri oluşturuluyor. Cart servisi henüz yok.

## Karar (Decision)
**Cart servisi oluşturulmayacak.** Sipariş oluştururken item'lar doğrudan request body'de gelecek.

Monolitteki PlaceOrderRequest:
```json
{"recipientName": "Ahmet", "shippingAddress": "İstanbul"}
// Item'lar sepetten otomatik alınıyordu
```

Mikroservisteki PlaceOrderRequest:
```json
{
  "recipientName": "Ahmet",
  "shippingAddress": "İstanbul",
  "items": [
    {"productId": "11111111-...", "quantity": 1},
    {"productId": "22222222-...", "quantity": 2}
  ]
}
// Item'lar client tarafından doğrudan gönderiliyor
```

Fiyat bilgisi client'tan ALINMAZ — order-service her item için product-service'e REST çağrı yaparak fiyatı doğrular. Bu, client'ın fiyat manipülasyonunu önler.

## Sonuçlar (Consequences)
**Pozitif:**
- Cart Service bağımlılığı ortadan kalkıyor — Saga pattern'e gerek yok
- Daha basit akış: Order → Product (doğrulama) → DB kaydet → Kafka event
- Cart Service ileride bağımsız olarak eklenebilir (frontend'de sepet yönetimi)
- Phase 1 kapsamı küçülüyor, daha hızlı teslim

**Negatif:**
- Kullanıcı deneyimi değişiyor: Frontend sepet yönetimini kendisi yapmalı (localStorage veya state)
- Indirim mekanizması henüz yok (Discount servisi olmadan kupon uygulanamaz)
- Monolitteki Cart → Order akışının doğal karşılığı kaybolmuş oluyor

## Uyumluluk (Compliance)
- `PlaceOrderRequest` item listesi içerecek (@NotEmpty validation)
- Her item için product-service'e fiyat doğrulama yapılacak (client fiyatına güvenilmez)
- İleride Cart servisi eklendiğinde, sipariş akışı Cart → Order Saga'ya dönüştürülebilir
- Bu ADR, Cart servisi eklendiğinde revize edilecek
