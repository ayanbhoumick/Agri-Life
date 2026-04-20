# AgriLife Spring Boot Web App Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert the existing plain Java CLI app into a Spring Boot + Thymeleaf web app with an agricultural theme, keeping all existing business logic intact.

**Architecture:** Spring Boot MVC — controller handles GET `/` (form) and POST `/recommend` (results). Existing `PestService` and `DeliveryService` become Spring `@Service` beans. Thymeleaf renders server-side HTML. Static CSS provides agricultural theme.

**Tech Stack:** Java 17, Spring Boot 3.2.5, Thymeleaf, Maven, HTML/CSS (no JS framework)

**Design Audit Rule (from `src/DESIGN.md`):** Before finalizing ANY file under `src/main/resources/static/` or `src/main/resources/templates/`, run all 5 audits in order: 01 Visual Hierarchy → 02 Typography → 03 Whitespace → 04 Color/Contrast → 05 Why Does This Look Cheap. Fix inline, then deliver.

---

## File Map

| Action | Path | Responsibility |
|--------|------|---------------|
| Create | `pom.xml` | Maven build config, Spring Boot + Thymeleaf deps |
| Move + annotate | `src/main/java/agrilife/Farmer.java` | POJO domain model |
| Move + annotate | `src/main/java/agrilife/PestService.java` | `@Service` — pest→pesticide mapping |
| Move + annotate | `src/main/java/agrilife/DeliveryService.java` | `@Service` — delivery time calc |
| Create | `src/main/java/agrilife/FarmerForm.java` | Form binding DTO (name, phone, pestName, distance, speed) |
| Create | `src/main/java/agrilife/AgriLifeApplication.java` | Spring Boot entry point |
| Create | `src/main/java/agrilife/AgriLifeController.java` | MVC controller — GET `/`, POST `/recommend` |
| Create | `src/main/resources/templates/index.html` | Form page (Thymeleaf) |
| Create | `src/main/resources/templates/result.html` | Results page (Thymeleaf) |
| Create | `src/main/resources/static/style.css` | Agricultural theme CSS |
| Create | `src/test/java/agrilife/PestServiceTest.java` | Unit tests for pest mapping |
| Create | `src/test/java/agrilife/DeliveryServiceTest.java` | Unit tests for delivery calc |
| Delete | `src/MainApp.java` | CLI entry point — replaced by web controller |

---

## Task 1: Create Maven project structure and pom.xml

**Files:**
- Create: `pom.xml` (project root: `/Users/abhoumic/Downloads/AgriLife/pom.xml`)
- Create dirs: `src/main/java/agrilife/`, `src/main/resources/templates/`, `src/main/resources/static/`, `src/test/java/agrilife/`

- [ ] **Step 1: Create directory structure**

```bash
mkdir -p /Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife
mkdir -p /Users/abhoumic/Downloads/AgriLife/src/main/resources/templates
mkdir -p /Users/abhoumic/Downloads/AgriLife/src/main/resources/static
mkdir -p /Users/abhoumic/Downloads/AgriLife/src/test/java/agrilife
```

- [ ] **Step 2: Create pom.xml**

Create `/Users/abhoumic/Downloads/AgriLife/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>

    <groupId>agrilife</groupId>
    <artifactId>agrilife</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: Verify Maven can download dependencies**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn dependency:resolve -q
```

Expected: `BUILD SUCCESS` (may take 1-2 min first run)

---

## Task 2: Move and annotate existing Java files

**Files:**
- Move: `src/Farmer.java` → `src/main/java/agrilife/Farmer.java`
- Move + annotate: `src/PestService.java` → `src/main/java/agrilife/PestService.java`
- Move + annotate: `src/DeliveryService.java` → `src/main/java/agrilife/DeliveryService.java`
- Delete: `src/MainApp.java`

- [ ] **Step 1: Copy Farmer.java (no changes needed)**

Copy `/Users/abhoumic/Downloads/AgriLife/src/Farmer.java` to `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/Farmer.java` with exact same content.

- [ ] **Step 2: Copy and annotate PestService.java**

Create `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/PestService.java`:

```java
package agrilife;

import org.springframework.stereotype.Service;

@Service
public class PestService {

    public String recommendPesticide(String pestName) {
        if (pestName == null || pestName.trim().isEmpty()) {
            return "No pest information provided. Please consult an expert.";
        }

        String pest = pestName.trim().toLowerCase();

        if (pest.equals("aphids") || pest.equals("aphid")) {
            return "Imidacloprid";
        } else if (pest.equals("armyworm") || pest.equals("army worm")) {
            return "Spinosad";
        } else if (pest.equals("whitefly") || pest.equals("white fly")) {
            return "Acetamiprid";
        } else if (pest.equals("thrips") || pest.equals("thrip")) {
            return "Abamectin";
        } else if (pest.equals("mites") || pest.equals("spider mites")) {
            return "Bifenazate";
        } else if (pest.equals("locusts") || pest.equals("locust")) {
            return "Chlorpyrifos";
        } else if (pest.equals("cutworm") || pest.equals("cut worm")) {
            return "Lambda-cyhalothrin";
        } else {
            return "Unknown pest. Please consult an agricultural expert.";
        }
    }
}
```

- [ ] **Step 3: Copy and annotate DeliveryService.java**

Create `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/DeliveryService.java`:

```java
package agrilife;

import org.springframework.stereotype.Service;

@Service
public class DeliveryService {

    private static final int VERIFICATION_TIME = 5;

    public double calculateDeliveryTime(double distanceKm, double speedKmh) {
        if (speedKmh <= 0) {
            throw new IllegalArgumentException("Speed must be greater than zero.");
        }
        if (distanceKm < 0) {
            throw new IllegalArgumentException("Distance cannot be negative.");
        }

        double travelTime = (distanceKm * 60) / speedKmh;
        return travelTime + VERIFICATION_TIME;
    }
}
```

- [ ] **Step 4: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add pom.xml src/main/java/agrilife/
git commit -m "feat: scaffold Spring Boot Maven structure, migrate existing services"
```

---

## Task 3: Create AgriLifeApplication entry point

**Files:**
- Create: `src/main/java/agrilife/AgriLifeApplication.java`

- [ ] **Step 1: Create AgriLifeApplication.java**

Create `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/AgriLifeApplication.java`:

```java
package agrilife;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AgriLifeApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgriLifeApplication.class, args);
    }
}
```

- [ ] **Step 2: Verify app starts (no controller yet, expect Whitelabel page)**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn spring-boot:run &
sleep 10
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/
```

Expected: `404` (no routes yet — proves Spring Boot started)

Stop the background process after verification: `mvn spring-boot:stop` or `kill %1`

- [ ] **Step 3: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/java/agrilife/AgriLifeApplication.java
git commit -m "feat: add Spring Boot application entry point"
```

---

## Task 4: Create FarmerForm DTO

**Files:**
- Create: `src/main/java/agrilife/FarmerForm.java`

- [ ] **Step 1: Create FarmerForm.java**

Create `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/FarmerForm.java`:

```java
package agrilife;

public class FarmerForm {

    private String name;
    private String phone;
    private String pestName;
    private double distance;
    private double speed;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPestName() { return pestName; }
    public void setPestName(String pestName) { this.pestName = pestName; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
}
```

- [ ] **Step 2: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/java/agrilife/FarmerForm.java
git commit -m "feat: add FarmerForm DTO for Thymeleaf form binding"
```

---

## Task 5: Write unit tests for PestService and DeliveryService

**Files:**
- Create: `src/test/java/agrilife/PestServiceTest.java`
- Create: `src/test/java/agrilife/DeliveryServiceTest.java`

- [ ] **Step 1: Create PestServiceTest.java**

Create `/Users/abhoumic/Downloads/AgriLife/src/test/java/agrilife/PestServiceTest.java`:

```java
package agrilife;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PestServiceTest {

    private final PestService service = new PestService();

    @Test
    void aphids_returns_imidacloprid() {
        assertEquals("Imidacloprid", service.recommendPesticide("Aphids"));
    }

    @Test
    void armyworm_returns_spinosad() {
        assertEquals("Spinosad", service.recommendPesticide("armyworm"));
    }

    @Test
    void whitefly_returns_acetamiprid() {
        assertEquals("Acetamiprid", service.recommendPesticide("Whitefly"));
    }

    @Test
    void thrips_returns_abamectin() {
        assertEquals("Abamectin", service.recommendPesticide("thrips"));
    }

    @Test
    void mites_returns_bifenazate() {
        assertEquals("Bifenazate", service.recommendPesticide("mites"));
    }

    @Test
    void locusts_returns_chlorpyrifos() {
        assertEquals("Chlorpyrifos", service.recommendPesticide("locusts"));
    }

    @Test
    void cutworm_returns_lambda_cyhalothrin() {
        assertEquals("Lambda-cyhalothrin", service.recommendPesticide("cutworm"));
    }

    @Test
    void unknown_pest_returns_consult_message() {
        String result = service.recommendPesticide("dragon");
        assertTrue(result.contains("Unknown pest"));
    }

    @Test
    void null_pest_returns_no_info_message() {
        String result = service.recommendPesticide(null);
        assertTrue(result.contains("No pest information"));
    }
}
```

- [ ] **Step 2: Create DeliveryServiceTest.java**

Create `/Users/abhoumic/Downloads/AgriLife/src/test/java/agrilife/DeliveryServiceTest.java`:

```java
package agrilife;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeliveryServiceTest {

    private final DeliveryService service = new DeliveryService();

    @Test
    void calculates_delivery_time_correctly() {
        // 30km at 60km/h = 30 min travel + 5 min verification = 35 min
        double result = service.calculateDeliveryTime(30, 60);
        assertEquals(35.0, result, 0.001);
    }

    @Test
    void zero_distance_returns_verification_time_only() {
        // 0km at any speed = 0 travel + 5 verification = 5 min
        double result = service.calculateDeliveryTime(0, 50);
        assertEquals(5.0, result, 0.001);
    }

    @Test
    void zero_speed_throws_illegal_argument() {
        assertThrows(IllegalArgumentException.class,
            () -> service.calculateDeliveryTime(10, 0));
    }

    @Test
    void negative_speed_throws_illegal_argument() {
        assertThrows(IllegalArgumentException.class,
            () -> service.calculateDeliveryTime(10, -5));
    }

    @Test
    void negative_distance_throws_illegal_argument() {
        assertThrows(IllegalArgumentException.class,
            () -> service.calculateDeliveryTime(-1, 60));
    }
}
```

- [ ] **Step 3: Run tests — verify they pass**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test
```

Expected: `Tests run: 14, Failures: 0, Errors: 0, BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/test/
git commit -m "test: add unit tests for PestService and DeliveryService"
```

---

## Task 6: Create AgriLifeController

**Files:**
- Create: `src/main/java/agrilife/AgriLifeController.java`

- [ ] **Step 1: Create AgriLifeController.java**

Create `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/AgriLifeController.java`:

```java
package agrilife;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AgriLifeController {

    private final PestService pestService;
    private final DeliveryService deliveryService;

    public AgriLifeController(PestService pestService, DeliveryService deliveryService) {
        this.pestService = pestService;
        this.deliveryService = deliveryService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("farmerForm", new FarmerForm());
        return "index";
    }

    @PostMapping("/recommend")
    public String recommend(@ModelAttribute FarmerForm form, Model model) {
        Farmer farmer = new Farmer(form.getName(), form.getPhone(), form.getPestName());
        String recommendation = pestService.recommendPesticide(farmer.getPestName());

        double deliveryTime;
        String deliveryError = null;
        try {
            deliveryTime = deliveryService.calculateDeliveryTime(form.getDistance(), form.getSpeed());
        } catch (IllegalArgumentException e) {
            deliveryTime = deliveryService.calculateDeliveryTime(form.getDistance(), 30);
            deliveryError = "Invalid speed — defaulted to 30 km/h.";
        }

        model.addAttribute("farmer", farmer);
        model.addAttribute("recommendation", recommendation);
        model.addAttribute("deliveryTime", String.format("%.1f", deliveryTime));
        model.addAttribute("distance", form.getDistance());
        model.addAttribute("speed", form.getSpeed());
        model.addAttribute("deliveryError", deliveryError);
        return "result";
    }
}
```

- [ ] **Step 2: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/java/agrilife/AgriLifeController.java
git commit -m "feat: add MVC controller with GET / and POST /recommend routes"
```

---

## Task 7: Create style.css (agricultural theme)

**Files:**
- Create: `src/main/resources/static/style.css`

Run all 5 design audits from `src/DESIGN.md` before finalizing this file.

- [ ] **Step 1: Create style.css and run design audits**

Create `/Users/abhoumic/Downloads/AgriLife/src/main/resources/static/style.css`:

```css
*, *::before, *::after {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

body {
    font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
    background-color: #f5f0e8;
    color: #2c2c2c;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
}

/* ── Header ── */
header {
    background-color: #1e4620;
    padding: 20px 40px;
    display: flex;
    align-items: center;
    gap: 16px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.3);
}

header .logo {
    font-size: 2rem;
}

header h1 {
    font-size: 1.6rem;
    font-weight: 700;
    color: #ffffff;
    letter-spacing: -0.5px;
}

header p.tagline {
    font-size: 0.85rem;
    color: #a8d5a2;
    margin-top: 2px;
    letter-spacing: 0.3px;
}

/* ── Main layout ── */
main {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: flex-start;
    padding: 48px 24px;
}

.card {
    background: #ffffff;
    border-radius: 12px;
    box-shadow: 0 4px 24px rgba(0,0,0,0.10);
    padding: 40px 48px;
    width: 100%;
    max-width: 560px;
}

.card h2 {
    font-size: 1.3rem;
    font-weight: 700;
    color: #1e4620;
    margin-bottom: 28px;
    padding-bottom: 12px;
    border-bottom: 2px solid #e8e0d0;
    letter-spacing: -0.3px;
}

/* ── Form ── */
.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    font-size: 0.82rem;
    font-weight: 600;
    color: #4a4a4a;
    text-transform: uppercase;
    letter-spacing: 0.6px;
    margin-bottom: 6px;
}

.form-group input,
.form-group select {
    width: 100%;
    padding: 11px 14px;
    border: 1.5px solid #d0c9bb;
    border-radius: 7px;
    font-size: 0.97rem;
    color: #2c2c2c;
    background-color: #fdfaf5;
    transition: border-color 0.2s, box-shadow 0.2s;
    appearance: none;
}

.form-group input:focus,
.form-group select:focus {
    outline: none;
    border-color: #2d7a32;
    box-shadow: 0 0 0 3px rgba(45, 122, 50, 0.15);
}

.form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
}

/* ── Button ── */
.btn-primary {
    display: block;
    width: 100%;
    padding: 13px;
    background-color: #2d7a32;
    color: #ffffff;
    font-size: 1rem;
    font-weight: 600;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    margin-top: 28px;
    letter-spacing: 0.2px;
    transition: background-color 0.2s, transform 0.1s;
}

.btn-primary:hover {
    background-color: #236227;
}

.btn-primary:active {
    transform: scale(0.99);
}

.btn-secondary {
    display: inline-block;
    padding: 10px 24px;
    background-color: transparent;
    color: #2d7a32;
    font-size: 0.95rem;
    font-weight: 600;
    border: 2px solid #2d7a32;
    border-radius: 8px;
    cursor: pointer;
    text-decoration: none;
    transition: background-color 0.2s, color 0.2s;
    margin-top: 24px;
}

.btn-secondary:hover {
    background-color: #2d7a32;
    color: #ffffff;
}

/* ── Result page ── */
.result-section {
    margin-bottom: 28px;
}

.result-section h3 {
    font-size: 0.78rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.8px;
    color: #7a6a4a;
    margin-bottom: 12px;
}

.result-row {
    display: flex;
    justify-content: space-between;
    align-items: baseline;
    padding: 9px 0;
    border-bottom: 1px solid #f0ebe0;
    font-size: 0.95rem;
}

.result-row:last-child {
    border-bottom: none;
}

.result-row .label {
    color: #6b6b6b;
    font-weight: 500;
}

.result-row .value {
    color: #1e1e1e;
    font-weight: 600;
    text-align: right;
}

.highlight-box {
    background: linear-gradient(135deg, #eaf5ea 0%, #f5f9f0 100%);
    border: 1.5px solid #b4ddb4;
    border-radius: 10px;
    padding: 20px 24px;
    margin-bottom: 28px;
}

.highlight-box .label {
    font-size: 0.78rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.8px;
    color: #3a7a3a;
    margin-bottom: 6px;
}

.highlight-box .value {
    font-size: 1.4rem;
    font-weight: 700;
    color: #1e4620;
}

.highlight-box .sub {
    font-size: 0.82rem;
    color: #5a8a5a;
    margin-top: 4px;
}

.error-note {
    font-size: 0.82rem;
    color: #b85c00;
    margin-top: 8px;
}

/* ── Divider ── */
.divider {
    border: none;
    border-top: 1.5px solid #e8e0d0;
    margin: 24px 0;
}

/* ── Footer ── */
footer {
    text-align: center;
    padding: 16px;
    font-size: 0.78rem;
    color: #9a9080;
    background-color: #ede8de;
}
```

- [ ] **Step 2: Run Design Audit 01 — Visual Hierarchy Surgeon**

Mentally assess: eye lands first on card title (h2, dark green, large), second on form labels, third on input fields. Business goal: farmer should immediately see the form and understand the call to action. No competing elements. Submit button is visually dominant via full-width green. Fix: none needed — hierarchy is clear.

- [ ] **Step 3: Run Design Audit 02 — Typography Interrogation**

PAIRING: single system font — no tension, appropriate for utilitarian ag tool.
SCALE: h1 (1.6rem) > h2 (1.3rem) > body (0.97rem) > labels (0.82rem uppercase) — clear contrast.
SPACING: line-height inherited from system defaults (adequate). Labels uppercase + 0.6px tracking creates clear distinction from input text.
WEIGHT: labels 600, values 600, body 400 — three clear tiers. No fixes needed.

- [ ] **Step 4: Run Design Audit 03 — Whitespace Pressure Test**

MACRO: header 20px padding, main 48px top padding, card 40/48px padding — generous.
MICRO: form-group 20px bottom margin, input 11/14px padding — not squeezed.
BREATHING: highlight-box (recommendation) isolated via background + border — feels important.
PERCEIVED VALUE: 48px card padding signals premium over cramped 16px. Good.

- [ ] **Step 5: Run Design Audit 04 — Color and Contrast Stress Test**

PALETTE: #1e4620 (dark green header), #2d7a32 (primary action), #f5f0e8 (warm page bg), #ffffff (card), #fdfaf5 (input bg), #7a6a4a (section labels). 6 colors — dominant/secondary/accent structure clear.
EMOTIONAL: earthy, trustworthy, agricultural — correct for audience.
ACCESSIBILITY: white on #1e4620 = high contrast (passes WCAG AA). White on #2d7a32 ≈ 4.7:1 (passes). Dark text on warm bg passes. Interactive inputs have visible focus ring.
SOPHISTICATION: green used only for header + CTA — not overused. No fixes needed.

- [ ] **Step 6: Run Design Audit 05 — Why Does This Look Cheap?**

DIAGNOSIS after prior audits: (1) No visual brand mark / logo beyond emoji — acceptable for college project. (2) Select dropdown uses native OS styling — add custom chevron via CSS if time permits. (3) No micro-animation on card load — acceptable scope.
ROOT CAUSE: none critical. Highest ROI fix: ensure font rendering is sharp (system-ui already handles this).
10X TREATMENT: (1) Custom select arrow icon (2) Subtle card entrance animation (3) Green gradient on header.
WHAT TO KEEP: highlight-box gradient — premium signal.

Final verdict: CSS passes all 5 audits. No blocking changes required.

- [ ] **Step 7: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/resources/static/style.css
git commit -m "feat: add agricultural theme CSS (passes 5-audit design review)"
```

---

## Task 8: Create index.html (form page)

**Files:**
- Create: `src/main/resources/templates/index.html`

Run all 5 design audits from `src/DESIGN.md` before finalizing.

- [ ] **Step 1: Create index.html**

Create `/Users/abhoumic/Downloads/AgriLife/src/main/resources/templates/index.html`:

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AgriLife — Pest Management System</title>
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
            <h2>Pest Report Form</h2>

            <form th:action="@{/recommend}" th:object="${farmerForm}" method="post">

                <div class="form-group">
                    <label for="name">Farmer Name</label>
                    <input type="text" id="name" th:field="*{name}"
                           placeholder="e.g. John Mwangi" required>
                </div>

                <div class="form-group">
                    <label for="phone">Phone Number</label>
                    <input type="tel" id="phone" th:field="*{phone}"
                           placeholder="e.g. +254 712 345 678" required>
                </div>

                <div class="form-group">
                    <label for="pestName">Pest Detected</label>
                    <select id="pestName" th:field="*{pestName}" required>
                        <option value="">— Select pest —</option>
                        <option value="aphids">Aphids</option>
                        <option value="armyworm">Armyworm</option>
                        <option value="whitefly">Whitefly</option>
                        <option value="thrips">Thrips</option>
                        <option value="mites">Mites / Spider Mites</option>
                        <option value="locusts">Locusts</option>
                        <option value="cutworm">Cutworm</option>
                    </select>
                </div>

                <hr class="divider">

                <div class="form-row">
                    <div class="form-group">
                        <label for="distance">Distance to Farm (km)</label>
                        <input type="number" id="distance" th:field="*{distance}"
                               min="0" step="0.1" placeholder="e.g. 15" required>
                    </div>
                    <div class="form-group">
                        <label for="speed">Delivery Speed (km/h)</label>
                        <input type="number" id="speed" th:field="*{speed}"
                               min="1" step="0.1" placeholder="e.g. 60" required>
                    </div>
                </div>

                <button type="submit" class="btn-primary">Get Recommendation →</button>
            </form>
        </div>
    </main>

    <footer>
        AgriLife Pest Management System &copy; 2026
    </footer>
</body>
</html>
```

- [ ] **Step 2: Run Design Audits 01–05 on index.html**

01 HIERARCHY: Eye → header (dark green, logo), then card title, then first input. Goal = form completion. Submit button (full-width green) anchors bottom — correct. No competing elements.

02 TYPOGRAPHY: Labels uppercase 0.82rem + 600 weight. Input 0.97rem regular. Clear 3-tier hierarchy. Placeholder text provides guidance without cluttering label.

03 WHITESPACE: Card max-width 560px keeps line lengths readable. form-group 20px bottom margin. form-row gap 16px. Divider separates farm info from delivery info — logical grouping.

04 COLOR/CONTRAST: Green submit button = primary CTA, only green element in form body. Select + inputs on #fdfaf5 background = warm, agricultural. Focus ring (#2d7a32 15% opacity) accessible.

05 CHEAP DIAGNOSIS: (1) Emoji logo — acceptable for college scope. (2) Native select dropdown styling — acceptable. (3) No form validation feedback styling — acceptable for MVP.
ROOT CAUSE: None blocking. KEEP: divider separating farmer info from delivery info — good UX grouping.

Audits pass. No blocking changes.

- [ ] **Step 3: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/resources/templates/index.html
git commit -m "feat: add Thymeleaf form page with agricultural theme"
```

---

## Task 9: Create result.html (results page)

**Files:**
- Create: `src/main/resources/templates/result.html`

Run all 5 design audits before finalizing.

- [ ] **Step 1: Create result.html**

Create `/Users/abhoumic/Downloads/AgriLife/src/main/resources/templates/result.html`:

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AgriLife — Recommendation</title>
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
            <h2>Recommendation Report</h2>

            <!-- Pesticide recommendation highlight -->
            <div class="highlight-box">
                <div class="label">Recommended Pesticide</div>
                <div class="value" th:text="${recommendation}">—</div>
                <div class="sub" th:text="'For: ' + ${farmer.pestName}">—</div>
            </div>

            <!-- Delivery estimate highlight -->
            <div class="highlight-box">
                <div class="label">Estimated Delivery Time</div>
                <div class="value" th:text="${deliveryTime} + ' minutes'">—</div>
                <div class="sub" th:text="${distance} + ' km at ' + ${speed} + ' km/h + 5 min verification'">—</div>
                <div class="error-note" th:if="${deliveryError != null}" th:text="${deliveryError}"></div>
            </div>

            <hr class="divider">

            <!-- Farmer details -->
            <div class="result-section">
                <h3>Farmer Details</h3>
                <div class="result-row">
                    <span class="label">Name</span>
                    <span class="value" th:text="${farmer.name}">—</span>
                </div>
                <div class="result-row">
                    <span class="label">Phone</span>
                    <span class="value" th:text="${farmer.phoneNumber}">—</span>
                </div>
                <div class="result-row">
                    <span class="label">Pest Reported</span>
                    <span class="value" th:text="${farmer.pestName}">—</span>
                </div>
            </div>

            <a href="/" class="btn-secondary">← New Report</a>
        </div>
    </main>

    <footer>
        AgriLife Pest Management System &copy; 2026
    </footer>
</body>
</html>
```

- [ ] **Step 2: Run Design Audits 01–05 on result.html**

01 HIERARCHY: Eye → pesticide highlight-box (green gradient, largest value text), then delivery box, then farmer details table. Correct — recommendation is the most important output.

02 TYPOGRAPHY: highlight-box value 1.4rem 700 = clear primary info. Section h3 uppercase 0.78rem = subtle category label. result-row label 500/value 600 — readable contrast.

03 WHITESPACE: Two highlight boxes separated by 28px gap. Divider creates zone before farmer details. Card padding 40/48px breathes. result-row 9px vertical padding — scannable.

04 COLOR: highlight-box uses green gradient (#eaf5ea→#f5f9f0) — ties pesticide/delivery outputs to brand. #b4ddb4 border subtle. error-note #b85c00 (amber) — visible warning, not alarming red.

05 CHEAP: (1) Back link uses `btn-secondary` — visible but not distracting from content. (2) No print/share action — out of scope. (3) No success icon or visual confirmation — acceptable MVP.
ROOT CAUSE: None blocking. KEEP: two-highlight-box layout — immediately communicates the two most important outputs.

Audits pass. No blocking changes.

- [ ] **Step 3: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/resources/templates/result.html
git commit -m "feat: add Thymeleaf result page with pesticide and delivery highlight"
```

---

## Task 10: End-to-end verification

- [ ] **Step 1: Build and run the app**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn spring-boot:run
```

Expected output includes: `Started AgriLifeApplication in X seconds`

- [ ] **Step 2: Open browser and test form submission**

Navigate to `http://localhost:8080/`

Verify:
- Form renders with all 7 pest options in dropdown
- Agricultural theme visible (dark green header, warm background)

- [ ] **Step 3: Submit a test case**

Fill in:
- Name: `Test Farmer`
- Phone: `+254 700 000 000`
- Pest: `Aphids`
- Distance: `30`
- Speed: `60`

Expected on result page:
- Recommended Pesticide: `Imidacloprid`
- Estimated Delivery Time: `35.0 minutes`
- Sub-label: `30.0 km at 60.0 km/h + 5 min verification`

- [ ] **Step 4: Test all 7 pests**

Submit form once for each pest. Verify mapping:
| Pest | Expected Pesticide |
|------|--------------------|
| Aphids | Imidacloprid |
| Armyworm | Spinosad |
| Whitefly | Acetamiprid |
| Thrips | Abamectin |
| Mites / Spider Mites | Bifenazate |
| Locusts | Chlorpyrifos |
| Cutworm | Lambda-cyhalothrin |

- [ ] **Step 5: Run full test suite one final time**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test
```

Expected: `BUILD SUCCESS`, 14 tests passing.

- [ ] **Step 6: Final commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add -A
git commit -m "feat: AgriLife Spring Boot web app complete — all 7 pests, delivery calc, agricultural theme"
```

---

## Self-Review

**Spec coverage check:**
- ✅ Spring Boot 3 + Thymeleaf → Tasks 1, 3, 6, 7, 8, 9
- ✅ Existing business logic kept → Task 2 (move + annotate services)
- ✅ Agricultural theme → Task 7 (CSS), audited in Tasks 8 + 9
- ✅ Form: name, phone, pest dropdown, distance, speed → Task 8
- ✅ Result: pesticide recommendation + delivery time → Task 9
- ✅ "New Report" back link → Task 9
- ✅ Tests → Task 5
- ✅ Design audits (DESIGN.md 5-pass) → Tasks 7, 8, 9

**Placeholder scan:** None found. All code blocks complete.

**Type consistency:** `FarmerForm` fields (name, phone, pestName, distance, speed) match Thymeleaf `th:field` refs in index.html. `farmer.name`, `farmer.phoneNumber`, `farmer.pestName` match `Farmer.java` getters. `recommendation`, `deliveryTime`, `distance`, `speed`, `deliveryError` model attributes match result.html `th:text` refs. Consistent throughout.
