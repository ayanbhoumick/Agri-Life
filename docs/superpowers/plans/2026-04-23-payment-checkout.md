# Payment Checkout Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a mock INR checkout flow that appears after the result page, charges pesticide cost + delivery cost, saves a `PaymentRecord` linked to the report, and exposes a `/payments` history page.

**Architecture:** `PaymentService` owns all pricing logic and persistence; `PaymentController` serves GET `/checkout`, POST `/checkout`, and GET `/payments`; a one-line change to `ReportService.save()` exposes the saved record's ID so `result.html` can pass it to the checkout link.

**Tech Stack:** Java 17, Spring Boot 3.2.5, Spring Data JPA, H2, Thymeleaf, JUnit 5, `@DataJpaTest`

---

## File Map

| Action | Path | Responsibility |
|--------|------|----------------|
| Create | `src/main/java/agrilife/PaymentRecord.java` | JPA entity — one row per completed payment |
| Create | `src/main/java/agrilife/PaymentRepository.java` | Spring Data repo — findAll ordered by paidAt desc |
| Create | `src/main/java/agrilife/PaymentService.java` | Price map, delivery cost calc, card masking, processPayment, getAllPayments |
| Create | `src/main/java/agrilife/PaymentController.java` | Thin controller: GET /checkout, POST /checkout, GET /payments |
| Create | `src/main/resources/templates/checkout.html` | Order summary + mock card form + success banner |
| Create | `src/main/resources/templates/payments.html` | Payment history table |
| Create | `src/test/java/agrilife/PaymentServiceTest.java` | Unit + integration tests for PaymentService |
| Modify | `src/main/java/agrilife/ReportService.java` | save() returns ReportRecord instead of void |
| Modify | `src/main/java/agrilife/AgriLifeController.java` | Capture saved record, add reportId to model |
| Modify | `src/main/resources/templates/result.html` | Add "Proceed to Checkout" button |
| Modify | `src/main/resources/templates/reports.html` | Add /payments nav link |
| Modify | `src/main/resources/static/style.css` | Checkout panel, success banner, payments table styles |

---

## Task 1: Create PaymentRecord entity

**Files:**
- Create: `src/main/java/agrilife/PaymentRecord.java`

- [ ] **Step 1: Create the entity**

```java
package agrilife;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_records")
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reportId;
    private String farmerName;
    private String cardholderName;
    private String maskedCard;
    private String pesticide;
    private double pesticidePrice;
    private double deliveryCost;
    private double total;
    private LocalDateTime paidAt;
    private String status;

    protected PaymentRecord() {}

    public PaymentRecord(Long reportId, String farmerName, String cardholderName,
                         String maskedCard, String pesticide, double pesticidePrice,
                         double deliveryCost) {
        this.reportId = reportId;
        this.farmerName = farmerName;
        this.cardholderName = cardholderName;
        this.maskedCard = maskedCard;
        this.pesticide = pesticide;
        this.pesticidePrice = pesticidePrice;
        this.deliveryCost = deliveryCost;
        this.total = pesticidePrice + deliveryCost;
        this.paidAt = LocalDateTime.now();
        this.status = "PAID";
    }

    public Long getId() { return id; }
    public Long getReportId() { return reportId; }
    public String getFarmerName() { return farmerName; }
    public String getCardholderName() { return cardholderName; }
    public String getMaskedCard() { return maskedCard; }
    public String getPesticide() { return pesticide; }
    public double getPesticidePrice() { return pesticidePrice; }
    public double getDeliveryCost() { return deliveryCost; }
    public double getTotal() { return total; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public String getStatus() { return status; }
}
```

- [ ] **Step 2: Create PaymentRepository**

```java
package agrilife;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentRecord, Long> {
    List<PaymentRecord> findAllByOrderByPaidAtDesc();
}
```

- [ ] **Step 3: Verify project still compiles**

```bash
mvn compile -q
```

Expected: BUILD SUCCESS with no errors.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/agrilife/PaymentRecord.java src/main/java/agrilife/PaymentRepository.java
git commit -m "feat: add PaymentRecord entity and PaymentRepository"
```

---

## Task 2: PaymentService — price map, delivery cost, card masking

**Files:**
- Create: `src/test/java/agrilife/PaymentServiceTest.java`
- Create: `src/main/java/agrilife/PaymentService.java`

- [ ] **Step 1: Write the failing tests**

Create `src/test/java/agrilife/PaymentServiceTest.java`:

```java
package agrilife;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PaymentServiceTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReportRepository reportRepository;

    private PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(paymentRepository);
    }

    // --- Price map ---

    @Test
    void price_imidacloprid_is_399() {
        assertEquals(399.0, service.getPesticidePrice("Imidacloprid"), 0.001);
    }

    @Test
    void price_spinosad_is_799() {
        assertEquals(799.0, service.getPesticidePrice("Spinosad"), 0.001);
    }

    @Test
    void price_acetamiprid_is_325() {
        assertEquals(325.0, service.getPesticidePrice("Acetamiprid"), 0.001);
    }

    @Test
    void price_abamectin_is_499() {
        assertEquals(499.0, service.getPesticidePrice("Abamectin"), 0.001);
    }

    @Test
    void price_bifenazate_is_699() {
        assertEquals(699.0, service.getPesticidePrice("Bifenazate"), 0.001);
    }

    @Test
    void price_chlorpyrifos_is_425() {
        assertEquals(425.0, service.getPesticidePrice("Chlorpyrifos"), 0.001);
    }

    @Test
    void price_lambda_cyhalothrin_is_375() {
        assertEquals(375.0, service.getPesticidePrice("Lambda-cyhalothrin"), 0.001);
    }

    @Test
    void price_buprofezin_is_549() {
        assertEquals(549.0, service.getPesticidePrice("Buprofezin"), 0.001);
    }

    @Test
    void price_chlorantraniliprole_is_1199() {
        assertEquals(1199.0, service.getPesticidePrice("Chlorantraniliprole"), 0.001);
    }

    @Test
    void price_emamectin_benzoate_is_649() {
        assertEquals(649.0, service.getPesticidePrice("Emamectin Benzoate"), 0.001);
    }

    @Test
    void price_cartap_hydrochloride_is_325() {
        assertEquals(325.0, service.getPesticidePrice("Cartap Hydrochloride"), 0.001);
    }

    @Test
    void price_dimethoate_is_325() {
        assertEquals(325.0, service.getPesticidePrice("Dimethoate"), 0.001);
    }

    @Test
    void price_unknown_pesticide_returns_zero() {
        assertEquals(0.0, service.getPesticidePrice("UnknownChemical"), 0.001);
    }

    // --- Delivery cost ---

    @Test
    void delivery_cost_10km_is_180() {
        assertEquals(180.0, service.calculateDeliveryCost(10.0), 0.001);
    }

    @Test
    void delivery_cost_25km_is_450() {
        assertEquals(450.0, service.calculateDeliveryCost(25.0), 0.001);
    }

    @Test
    void delivery_cost_zero_distance_is_zero() {
        assertEquals(0.0, service.calculateDeliveryCost(0.0), 0.001);
    }

    // --- Card masking ---

    @Test
    void maskCard_returns_last_four_with_asterisks() {
        assertEquals("**** 4242", service.maskCard("1234567890124242"));
    }

    @Test
    void maskCard_returns_last_four_when_short_input() {
        assertEquals("**** 99", service.maskCard("99"));
    }

    // --- processPayment ---

    @Test
    void processPayment_saves_record_to_db() {
        service.processPayment(1L, "Ravi Kumar", "Ravi Kumar", "4111111111111234",
                "Imidacloprid", 20.0);
        List<PaymentRecord> all = paymentRepository.findAll();
        assertEquals(1, all.size());
    }

    @Test
    void processPayment_stores_masked_card() {
        service.processPayment(1L, "Ravi Kumar", "Ravi Kumar", "4111111111111234",
                "Imidacloprid", 20.0);
        PaymentRecord saved = paymentRepository.findAll().get(0);
        assertEquals("**** 1234", saved.getMaskedCard());
    }

    @Test
    void processPayment_calculates_correct_total() {
        // Imidacloprid=399, distance=10 → deliveryCost=180 → total=579
        service.processPayment(1L, "Ravi Kumar", "Ravi Kumar", "4111111111111234",
                "Imidacloprid", 10.0);
        PaymentRecord saved = paymentRepository.findAll().get(0);
        assertEquals(579.0, saved.getTotal(), 0.001);
    }

    @Test
    void processPayment_status_is_PAID() {
        service.processPayment(1L, "Ravi Kumar", "Ravi Kumar", "4111111111111234",
                "Imidacloprid", 10.0);
        PaymentRecord saved = paymentRepository.findAll().get(0);
        assertEquals("PAID", saved.getStatus());
    }

    // --- getAllPayments ---

    @Test
    void getAllPayments_returns_newest_first() throws InterruptedException {
        service.processPayment(1L, "First", "First", "0000000000001111", "Imidacloprid", 10.0);
        Thread.sleep(20);
        service.processPayment(2L, "Second", "Second", "0000000000002222", "Spinosad", 5.0);
        List<PaymentRecord> all = service.getAllPayments();
        assertEquals("Second", all.get(0).getFarmerName());
        assertEquals("First", all.get(1).getFarmerName());
    }
}
```

- [ ] **Step 2: Run tests to confirm they fail**

```bash
mvn test -Dtest=PaymentServiceTest -q 2>&1 | tail -20
```

Expected: COMPILATION ERROR — `PaymentService` does not exist yet.

- [ ] **Step 3: Create PaymentService**

Create `src/main/java/agrilife/PaymentService.java`:

```java
package agrilife;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    private static final Map<String, Double> PRICE_MAP = new HashMap<>();
    private static final double DELIVERY_RATE = 18.0;

    static {
        PRICE_MAP.put("Imidacloprid",        399.0);
        PRICE_MAP.put("Spinosad",            799.0);
        PRICE_MAP.put("Acetamiprid",         325.0);
        PRICE_MAP.put("Abamectin",           499.0);
        PRICE_MAP.put("Bifenazate",          699.0);
        PRICE_MAP.put("Chlorpyrifos",        425.0);
        PRICE_MAP.put("Lambda-cyhalothrin",  375.0);
        PRICE_MAP.put("Buprofezin",          549.0);
        PRICE_MAP.put("Chlorantraniliprole", 1199.0);
        PRICE_MAP.put("Emamectin Benzoate",  649.0);
        PRICE_MAP.put("Cartap Hydrochloride",325.0);
        PRICE_MAP.put("Dimethoate",          325.0);
    }

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public double getPesticidePrice(String pesticide) {
        return PRICE_MAP.getOrDefault(pesticide, 0.0);
    }

    public double calculateDeliveryCost(double distance) {
        return distance * DELIVERY_RATE;
    }

    public String maskCard(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "**** " + (cardNumber == null ? "" : cardNumber);
        }
        return "**** " + cardNumber.substring(cardNumber.length() - 4);
    }

    public void processPayment(Long reportId, String farmerName, String cardholderName,
                               String cardNumber, String pesticide, double distance) {
        double pesticidePrice = getPesticidePrice(pesticide);
        double deliveryCost = calculateDeliveryCost(distance);
        String masked = maskCard(cardNumber);
        PaymentRecord record = new PaymentRecord(reportId, farmerName, cardholderName,
                masked, pesticide, pesticidePrice, deliveryCost);
        paymentRepository.save(record);
    }

    public List<PaymentRecord> getAllPayments() {
        return paymentRepository.findAllByOrderByPaidAtDesc();
    }
}
```

- [ ] **Step 4: Run tests and confirm they pass**

```bash
mvn test -Dtest=PaymentServiceTest -q 2>&1 | tail -5
```

Expected: `BUILD SUCCESS`, 21 tests passed, 0 failures.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/agrilife/PaymentService.java src/test/java/agrilife/PaymentServiceTest.java
git commit -m "feat: add PaymentService with price map, delivery cost, and processPayment"
```

---

## Task 3: Modify ReportService.save() to return ReportRecord

Right now `ReportService.save()` returns `void`. The result page needs to pass the saved report's ID to the checkout link. This task changes the return type and threads the ID through to the template.

**Files:**
- Modify: `src/main/java/agrilife/ReportService.java`
- Modify: `src/main/java/agrilife/AgriLifeController.java`

- [ ] **Step 1: Change ReportService.save() to return ReportRecord**

In `src/main/java/agrilife/ReportService.java`, change:

```java
public void save(FarmerForm form, String pesticide, double deliveryTime, double effectiveSpeed) {
    ReportRecord record = new ReportRecord(
        form.getName(), form.getPhone(), form.getCropName(), form.getPestName(),
        pesticide, deliveryTime, form.getDistance(), effectiveSpeed, form.getLocation()
    );
    repository.save(record);
}
```

to:

```java
public ReportRecord save(FarmerForm form, String pesticide, double deliveryTime, double effectiveSpeed) {
    ReportRecord record = new ReportRecord(
        form.getName(), form.getPhone(), form.getCropName(), form.getPestName(),
        pesticide, deliveryTime, form.getDistance(), effectiveSpeed, form.getLocation()
    );
    return repository.save(record);
}
```

- [ ] **Step 2: Update AgriLifeController to capture the returned record**

In `src/main/java/agrilife/AgriLifeController.java`, find the line:

```java
reportService.save(form, recommendation, deliveryTime, effectiveSpeed);
```

Replace with:

```java
ReportRecord savedReport = reportService.save(form, recommendation, deliveryTime, effectiveSpeed);
```

Then add the following line right before `return "result";`:

```java
model.addAttribute("reportId", savedReport.getId());
```

- [ ] **Step 3: Run existing tests to verify no regressions**

```bash
mvn test -q 2>&1 | tail -5
```

Expected: `BUILD SUCCESS`, all existing tests pass. (`ReportServiceTest` doesn't assert on the return value, so it still compiles and passes.)

- [ ] **Step 4: Commit**

```bash
git add src/main/java/agrilife/ReportService.java src/main/java/agrilife/AgriLifeController.java
git commit -m "feat: return ReportRecord from save() and expose reportId on result model"
```

---

## Task 4: Add "Proceed to Checkout" button to result.html

**Files:**
- Modify: `src/main/resources/templates/result.html`

- [ ] **Step 1: Add checkout button after the existing nav links**

In `src/main/resources/templates/result.html`, find:

```html
<a th:href="@{/}" class="btn-secondary">← New Report</a>
<a th:href="@{/reports}" class="report-history-link">View All Reports →</a>
```

Replace with:

```html
<a th:href="@{/}" class="btn-secondary">← New Report</a>
<div class="result-nav-links">
    <a th:href="@{/reports}" class="report-history-link">View All Reports →</a>
    <a th:href="@{/checkout(reportId=${reportId})}" class="checkout-link">Proceed to Checkout →</a>
</div>
```

- [ ] **Step 2: Start the app and verify the button appears**

```bash
mvn spring-boot:run -q &
sleep 5
curl -s http://localhost:8080 | grep -c "AgriLife"
```

Expected: returns `1` (page loads). Then open `http://localhost:8080` in a browser, submit a report, and confirm "Proceed to Checkout →" appears on the result page. Stop the server: `kill %1`

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/templates/result.html
git commit -m "feat: add Proceed to Checkout link on result page"
```

---

## Task 5: Create PaymentController and checkout.html

**Files:**
- Create: `src/main/java/agrilife/PaymentController.java`
- Create: `src/main/resources/templates/checkout.html`

- [ ] **Step 1: Create PaymentController**

Create `src/main/java/agrilife/PaymentController.java`:

```java
package agrilife;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentController {

    private final ReportRepository reportRepository;
    private final PaymentService paymentService;

    public PaymentController(ReportRepository reportRepository, PaymentService paymentService) {
        this.reportRepository = reportRepository;
        this.paymentService = paymentService;
    }

    @GetMapping("/checkout")
    public String checkout(@RequestParam Long reportId,
                           @RequestParam(required = false) Boolean success,
                           Model model) {
        ReportRecord report = reportRepository.findById(reportId).orElse(null);
        if (report == null) {
            return "redirect:/";
        }
        double pesticidePrice = paymentService.getPesticidePrice(report.getPesticide());
        double deliveryCost   = paymentService.calculateDeliveryCost(report.getDistance());
        double total          = pesticidePrice + deliveryCost;

        model.addAttribute("report", report);
        model.addAttribute("pesticidePrice", pesticidePrice);
        model.addAttribute("deliveryCost", deliveryCost);
        model.addAttribute("total", total);
        model.addAttribute("success", Boolean.TRUE.equals(success));
        return "checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(@RequestParam Long reportId,
                                  @RequestParam String cardholderName,
                                  @RequestParam String cardNumber,
                                  @RequestParam String expiry,
                                  @RequestParam String cvv) {
        ReportRecord report = reportRepository.findById(reportId).orElse(null);
        if (report == null) {
            return "redirect:/";
        }
        paymentService.processPayment(
            reportId,
            report.getFarmerName(),
            cardholderName,
            cardNumber,
            report.getPesticide(),
            report.getDistance()
        );
        return "redirect:/checkout?reportId=" + reportId + "&success=true";
    }

    @GetMapping("/payments")
    public String payments(Model model) {
        model.addAttribute("payments", paymentService.getAllPayments());
        return "payments";
    }
}
```

- [ ] **Step 2: Create checkout.html**

Create `src/main/resources/templates/checkout.html`:

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AgriLife — Checkout</title>
    <link rel="stylesheet" th:href="@{/style.css}">
</head>
<body>
    <header>
        <span class="logo">🌾</span>
        <div>
            <h1>AgriLife</h1>
            <p class="tagline">Pest Management &amp; Delivery Estimation</p>
        </div>
    </header>

    <main>
        <div class="card">
            <h2>Checkout</h2>

            <!-- Success banner -->
            <div class="success-banner" th:if="${success}">
                ✅ Payment successful! Your order has been placed.
            </div>

            <!-- Order Summary -->
            <div class="checkout-section">
                <h3 class="checkout-section-title">Order Summary</h3>
                <div class="order-line">
                    <span class="order-label">Farmer</span>
                    <span class="order-value" th:text="${report.farmerName}">—</span>
                </div>
                <div class="order-line">
                    <span class="order-label">Crop</span>
                    <span class="order-value" th:text="${report.cropName}">—</span>
                </div>
                <div class="order-line">
                    <span class="order-label">Pest</span>
                    <span class="order-value" th:text="${report.pestName}">—</span>
                </div>
                <div class="order-line">
                    <span class="order-label" th:text="${report.pesticide}">Pesticide</span>
                    <span class="order-value" th:text="'₹' + ${#numbers.formatDecimal(pesticidePrice, 1, 0)}">—</span>
                </div>
                <div class="order-line">
                    <span class="order-label" th:text="'Delivery (' + ${report.distance} + ' km)'">Delivery</span>
                    <span class="order-value" th:text="'₹' + ${#numbers.formatDecimal(deliveryCost, 1, 0)}">—</span>
                </div>
                <div class="order-total-line">
                    <span class="order-label">Total</span>
                    <span class="order-total-value" th:text="'₹' + ${#numbers.formatDecimal(total, 1, 0)}">—</span>
                </div>
            </div>

            <hr class="divider">

            <!-- Payment Form -->
            <div class="checkout-section" th:if="${!success}">
                <h3 class="checkout-section-title">Payment Details</h3>
                <form th:action="@{/checkout}" method="post">
                    <input type="hidden" name="reportId" th:value="${report.id}">

                    <div class="form-group">
                        <label for="cardholderName">Cardholder Name</label>
                        <input type="text" id="cardholderName" name="cardholderName"
                               placeholder="As shown on card" required>
                    </div>

                    <div class="form-group">
                        <label for="cardNumber">Card Number</label>
                        <input type="text" id="cardNumber" name="cardNumber"
                               placeholder="1234 5678 9012 3456" maxlength="19" required>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="expiry">Expiry (MM/YY)</label>
                            <input type="text" id="expiry" name="expiry"
                                   placeholder="MM/YY" maxlength="5" required>
                        </div>
                        <div class="form-group">
                            <label for="cvv">CVV</label>
                            <input type="text" id="cvv" name="cvv"
                                   placeholder="123" maxlength="3" required>
                        </div>
                    </div>

                    <button type="submit" class="btn-primary"
                            th:text="'Pay ₹' + ${#numbers.formatDecimal(total, 1, 0)}">
                        Pay
                    </button>
                </form>
            </div>

            <div class="checkout-nav">
                <a th:href="@{/checkout(reportId=${report.id})}" class="btn-secondary"
                   th:if="${success}">View Order</a>
                <a th:href="@{/}" class="report-history-link">← New Report</a>
                <a th:href="@{/payments}" class="report-history-link">Payment History →</a>
            </div>
        </div>
    </main>

    <footer>
        AgriLife Pest Management System &copy; 2026
    </footer>
</body>
</html>
```

- [ ] **Step 3: Run all tests**

```bash
mvn test -q 2>&1 | tail -5
```

Expected: `BUILD SUCCESS`, all tests pass.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/agrilife/PaymentController.java src/main/resources/templates/checkout.html
git commit -m "feat: add PaymentController and checkout template"
```

---

## Task 6: Create payments.html

**Files:**
- Create: `src/main/resources/templates/payments.html`

- [ ] **Step 1: Create payments.html**

Create `src/main/resources/templates/payments.html`:

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AgriLife — Payment History</title>
    <link rel="stylesheet" th:href="@{/style.css}">
</head>
<body>
    <header>
        <span class="logo">🌾</span>
        <div>
            <h1>AgriLife</h1>
            <p class="tagline">Pest Management &amp; Delivery Estimation</p>
        </div>
    </header>

    <main>
        <div class="card">
            <div class="report-title-row">
                <h2>Payment History</h2>
                <span class="report-count"
                      th:text="${payments.size()} + (${payments.size()} == 1 ? ' payment' : ' payments')">
                    0 payments
                </span>
            </div>

            <!-- Empty state -->
            <div th:if="${payments.isEmpty()}" class="report-empty">
                No payments yet.
                <a th:href="@{/}">Submit a report to get started →</a>
            </div>

            <!-- Payments table -->
            <div th:if="${!payments.isEmpty()}" class="report-table">
                <div class="report-header-row payments-header-row">
                    <span>Date</span>
                    <span>Farmer</span>
                    <span>Pesticide</span>
                    <span>Delivery</span>
                    <span>Total</span>
                    <span>Status</span>
                </div>
                <div th:each="p, stat : ${payments}"
                     th:classappend="${stat.even} ? 'even' : ''"
                     class="report-row payments-row">
                    <div class="report-row-summary payments-row-summary">
                        <span th:text="${#temporals.format(p.paidAt, 'dd MMM yyyy')}">—</span>
                        <span th:text="${p.farmerName}">—</span>
                        <span th:text="${p.pesticide}">—</span>
                        <span th:text="'₹' + ${#numbers.formatDecimal(p.deliveryCost, 1, 0)}">—</span>
                        <span class="order-total-value" th:text="'₹' + ${#numbers.formatDecimal(p.total, 1, 0)}">—</span>
                        <span class="paid-badge" th:text="${p.status}">PAID</span>
                    </div>
                </div>
            </div>

            <a th:href="@{/}" class="btn-secondary">← New Report</a>
            <a th:href="@{/reports}" class="report-history-link">View Reports →</a>
        </div>
    </main>

    <footer>
        AgriLife Pest Management System &copy; 2026
    </footer>
</body>
</html>
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/templates/payments.html
git commit -m "feat: add payment history page at /payments"
```

---

## Task 7: Add CSS for checkout and payments pages

**Files:**
- Modify: `src/main/resources/static/style.css`

- [ ] **Step 1: Append new styles to style.css**

Add the following block at the end of `src/main/resources/static/style.css`:

```css
/* --- Checkout page --- */

.checkout-section {
    margin-bottom: 28px;
}

.checkout-section-title {
    font-size: 0.78rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.8px;
    color: #7a6a4a;
    margin-bottom: 14px;
}

.order-line {
    display: flex;
    justify-content: space-between;
    align-items: baseline;
    padding: 8px 0;
    border-bottom: 1px solid #f0ebe0;
    font-size: 0.95rem;
}

.order-line:last-of-type {
    border-bottom: none;
}

.order-label {
    color: #6b6b6b;
    font-weight: 500;
}

.order-value {
    color: #1e1e1e;
    font-weight: 600;
}

.order-total-line {
    display: flex;
    justify-content: space-between;
    align-items: baseline;
    padding: 12px 0 4px;
    font-size: 1rem;
    font-weight: 700;
    margin-top: 4px;
    border-top: 2px solid #e8e0d0;
}

.order-total-value {
    color: #1e4620;
    font-weight: 700;
}

.success-banner {
    background: #eaf5ea;
    border: 1.5px solid #7ac47a;
    border-radius: 8px;
    padding: 14px 20px;
    color: #1e4620;
    font-weight: 600;
    font-size: 0.97rem;
    margin-bottom: 24px;
}

.checkout-nav {
    margin-top: 28px;
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    align-items: center;
}

/* --- Payments history page --- */

.payments-header-row {
    grid-template-columns: 1.4fr 1.2fr 1.4fr 0.9fr 0.9fr 0.7fr !important;
}

.payments-row .report-row-summary,
.payments-row-summary {
    display: grid;
    grid-template-columns: 1.4fr 1.2fr 1.4fr 0.9fr 0.9fr 0.7fr;
    gap: 8px;
    align-items: center;
    padding: 12px 0;
    font-size: 0.93rem;
}

.paid-badge {
    display: inline-block;
    background: #eaf5ea;
    color: #1e4620;
    border: 1px solid #7ac47a;
    border-radius: 5px;
    padding: 2px 8px;
    font-size: 0.78rem;
    font-weight: 700;
    letter-spacing: 0.4px;
}

/* --- result.html nav --- */

.result-nav-links {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    align-items: center;
    margin-top: 4px;
}

.checkout-link {
    display: inline-block;
    padding: 10px 24px;
    background-color: #2d7a32;
    color: #ffffff;
    font-size: 0.95rem;
    font-weight: 600;
    border-radius: 8px;
    text-decoration: none;
    transition: background-color 0.2s;
}

.checkout-link:hover {
    background-color: #1e4620;
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/static/style.css
git commit -m "feat: add CSS for checkout panel, success banner, payments table, and checkout link"
```

---

## Task 8: Add /payments nav link to reports.html

**Files:**
- Modify: `src/main/resources/templates/reports.html`

- [ ] **Step 1: Add payments link to reports page**

In `src/main/resources/templates/reports.html`, find:

```html
<a th:href="@{/}" class="btn-secondary">← New Report</a>
```

Replace with:

```html
<a th:href="@{/}" class="btn-secondary">← New Report</a>
<a th:href="@{/payments}" class="report-history-link">Payment History →</a>
```

- [ ] **Step 2: Run all tests one final time**

```bash
mvn test -q 2>&1 | tail -5
```

Expected: `BUILD SUCCESS`, all tests pass.

- [ ] **Step 3: Smoke test the full flow**

```bash
mvn spring-boot:run -q &
sleep 6
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080
```

Expected: `200`. Then in browser:
1. Open `http://localhost:8080`, submit a report
2. Click "Proceed to Checkout →" — verify order summary shows correct ₹ amounts
3. Fill in mock card details, click Pay — verify green success banner appears
4. Open `http://localhost:8080/payments` — verify payment appears in table
5. Open `http://localhost:8080/reports` — verify "Payment History →" link is present

Stop server: `kill %1`

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/templates/reports.html
git commit -m "feat: add Payment History nav link to reports page"
```

---

## Self-Review Checklist

- [x] **Spec coverage:** PaymentRecord entity ✓ · price map all 12 pesticides ✓ · delivery ₹18/km ✓ · card masking ✓ · checkout GET/POST ✓ · success banner ✓ · /payments page ✓ · reportId from ReportService ✓ · nav links ✓
- [x] **Placeholder scan:** No TBD, no "implement later", all code blocks complete
- [x] **Type consistency:** `PaymentRecord` fields match constructor signature in Task 1; `PaymentService.processPayment()` signature called identically in Task 2 tests and Task 5 controller; `report.getDistance()` returns `double` — confirmed from `ReportRecord.java`
