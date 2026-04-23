# Payment Checkout Feature — Design Spec

**Date:** 2026-04-23
**Project:** AgriLife

---

## Overview

After the result page displays a pesticide recommendation and delivery estimate, the farmer can proceed to a mock checkout flow. The flow collects a simulated card payment covering both the pesticide cost and delivery cost, persists a `PaymentRecord` linked to the existing `ReportRecord`, and exposes a `/payments` history page.

---

## Data Model

### `PaymentRecord` (new JPA entity)

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | PK, auto-generated |
| `reportId` | `Long` | References `ReportRecord.id` |
| `cardholderName` | `String` | From checkout form |
| `maskedCard` | `String` | Last 4 digits only (e.g. `**** 4242`) |
| `pesticide` | `String` | Copied from report at payment time |
| `pesticidePrice` | `double` | Fixed price from `PaymentService` map |
| `deliveryCost` | `double` | `distance × 18.0` |
| `total` | `double` | `pesticidePrice + deliveryCost` |
| `paidAt` | `LocalDateTime` | Timestamp of submission |
| `status` | `String` | Always `"PAID"` (mock) |

### `PaymentRepository`

Extends `JpaRepository<PaymentRecord, Long>`. Adds `findAllByOrderByPaidAtDesc()`.

---

## Pricing

### Pesticide Prices (₹, Indian retail market)

| Pesticide | Pack | Price (₹) |
|---|---|---|
| Imidacloprid | 250 ml | 399 |
| Spinosad | 100 ml | 799 |
| Acetamiprid | 100 g | 325 |
| Abamectin | 250 ml | 499 |
| Bifenazate | 500 ml | 699 |
| Chlorpyrifos | 1 L | 425 |
| Lambda-cyhalothrin | 1 L | 375 |
| Buprofezin | 500 ml | 549 |
| Chlorantraniliprole | 150 ml | 1199 |
| Emamectin Benzoate | 100 g | 649 |
| Cartap Hydrochloride | 1 kg | 325 |
| Dimethoate | 1 L | 325 |

### Delivery Cost

`₹18 per km × distance` (rural last-mile rate, India).

---

## Components

### New Files

| File | Purpose |
|---|---|
| `PaymentRecord.java` | JPA entity |
| `PaymentRepository.java` | Spring Data repository |
| `PaymentService.java` | Price lookup map, total calc, save logic |
| `PaymentController.java` | GET `/checkout`, POST `/checkout`, GET `/payments` |
| `checkout.html` | Order summary + mock payment form |
| `payments.html` | Payment history table |

### Modified Files

| File | Change |
|---|---|
| `result.html` | Add "Proceed to Checkout" button linking to `/checkout?reportId={id}` |
| `index.html` / nav | Add `/payments` nav link alongside `/reports` |

---

## Flow

```
result.html
  → "Proceed to Checkout" button
  → GET /checkout?reportId=X
      PaymentController: loads ReportRecord, calls PaymentService.buildSummary()
      Model: farmerName, crop, pest, pesticide, pesticidePrice, deliveryKm,
             deliveryCost, total, reportId
  → checkout.html rendered

checkout.html (POST /checkout)
  Form fields: cardholderName, cardNumber, expiry, cvv, reportId (hidden)
  → PaymentController:
      1. Load ReportRecord by reportId
      2. Call PaymentService.processPayment(reportId, cardholderName, cardNumber, pesticide, distance)
         - Mask card → last 4 digits
         - Look up pesticidePrice
         - Calc deliveryCost = distance × 18
         - Save PaymentRecord
      3. Redirect → /checkout?reportId=X&success=true
  → checkout.html shows green "Payment successful! Order placed." banner

GET /payments
  → PaymentController loads all PaymentRecords desc by paidAt
  → payments.html table: date, farmer, pesticide, distance, total, status
```

---

## UI

### `checkout.html`

Two-panel layout (order summary + payment form):

**Order Summary panel:**
- Farmer name, crop, pest
- Pesticide: name — ₹price
- Delivery: Xkm — ₹cost
- **Total: ₹X** (bold)

**Mock Payment Form panel:**
- Cardholder Name (text)
- Card Number (text, 16-digit display only — no real validation)
- Expiry MM/YY (text)
- CVV (text, 3-digit)
- Submit button: `Pay ₹X`

On `?success=true`: green confirmation banner above form — "Payment successful! Order placed."

### `payments.html`

Table columns: Date | Farmer | Pesticide | Distance | Total | Status

Status always renders as `✅ PAID`.

---

## Error Handling

- If `reportId` not found on GET `/checkout`: redirect to `/` with no error (graceful fallback).
- Card fields are not validated server-side (mock flow). All non-empty submissions succeed.
- `distance` stored as `double` in `ReportRecord` — used directly in delivery calc.

---

## Testing

- `PaymentServiceTest`: verify price lookup for all 12 pesticides, delivery cost calc, masking logic.
- `PaymentControllerTest` (Spring MVC): GET `/checkout` returns 200 with model attributes; POST `/checkout` saves record and redirects with `?success=true`; GET `/payments` returns list.
