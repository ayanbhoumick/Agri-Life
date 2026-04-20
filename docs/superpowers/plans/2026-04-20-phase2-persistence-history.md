# AgriLife Phase 2: Persistence + Report History Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Persist every farmer submission to an embedded H2 database and expose a read-only history page at `/reports` with inline accordion row expansion.

**Architecture:** Spring Data JPA + H2 in-memory database. One `@Entity` (`ReportRecord`) maps to an auto-created table. A `JpaRepository` handles DB access. A `@Service` wraps save/query. A new `@Controller` serves GET /reports. The existing `AgriLifeController` saves a record after each POST /recommend.

**Tech Stack:** Java 17, Spring Boot 3.2.5, Spring Data JPA, H2 (runtime), Thymeleaf, Vanilla JS

---

## File Map

| Action | Path | Responsibility |
|--------|------|---------------|
| Modify | `pom.xml` | Add spring-boot-starter-data-jpa + h2 |
| Create | `src/main/resources/application.properties` | H2 datasource + JPA config |
| Create | `src/main/java/agrilife/ReportRecord.java` | `@Entity` — one row per submission |
| Create | `src/main/java/agrilife/ReportRepository.java` | `JpaRepository` with `findAllByOrderByCreatedAtDesc()` |
| Create | `src/main/java/agrilife/ReportService.java` | `@Service` — `save()` + `getAllReports()` |
| Create | `src/test/java/agrilife/ReportServiceTest.java` | `@DataJpaTest` — save + retrieve + ordering |
| Modify | `src/main/java/agrilife/AgriLifeController.java` | Inject ReportService, call `save()` in POST /recommend |
| Create | `src/main/java/agrilife/ReportsController.java` | `@Controller` — GET /reports |
| Modify | `src/main/resources/static/style.css` | Report table, accordion, report history link |
| Create | `src/main/resources/templates/reports.html` | History table with inline expand + empty state |
| Modify | `src/main/resources/templates/result.html` | Add "View All Reports →" link |

---

## Task 1: Add JPA + H2 dependencies and application.properties

**Files:**
- Modify: `pom.xml`
- Create: `src/main/resources/application.properties`

- [ ] **Step 1: Add dependencies to pom.xml**

Read `/Users/abhoumic/Downloads/AgriLife/pom.xml` first. Then replace the `<dependencies>` block with:

```xml
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
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

- [ ] **Step 2: Create application.properties**

Create `/Users/abhoumic/Downloads/AgriLife/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:agrilife;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

- [ ] **Step 3: Verify compile**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn compile -q 2>&1 | tail -5
```

Expected: `BUILD SUCCESS` (Maven downloads JPA + H2 jars on first run — may take 30–60 seconds)

- [ ] **Step 4: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add pom.xml src/main/resources/application.properties
git commit -m "chore: add Spring Data JPA and H2 dependencies"
```

---

## Task 2: Create ReportRecord entity

**Files:**
- Create: `src/main/java/agrilife/ReportRecord.java`

- [ ] **Step 1: Create ReportRecord.java**

Create `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/ReportRecord.java`:

```java
package agrilife;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_records")
public class ReportRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String farmerName;
    private String phone;
    private String cropName;
    private String pestName;
    private String pesticide;
    private double deliveryTime;
    private double distance;
    private double speed;
    private LocalDateTime createdAt;

    protected ReportRecord() {}

    public ReportRecord(String farmerName, String phone, String cropName, String pestName,
                        String pesticide, double deliveryTime, double distance, double speed) {
        this.farmerName = farmerName;
        this.phone = phone;
        this.cropName = cropName;
        this.pestName = pestName;
        this.pesticide = pesticide;
        this.deliveryTime = deliveryTime;
        this.distance = distance;
        this.speed = speed;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getFarmerName() { return farmerName; }
    public String getPhone() { return phone; }
    public String getCropName() { return cropName; }
    public String getPestName() { return pestName; }
    public String getPesticide() { return pesticide; }
    public double getDeliveryTime() { return deliveryTime; }
    public double getDistance() { return distance; }
    public double getSpeed() { return speed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

- [ ] **Step 2: Verify compile**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn compile -q 2>&1 | tail -3
```

Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/java/agrilife/ReportRecord.java
git commit -m "feat: add ReportRecord JPA entity"
```

---

## Task 3: Create ReportRepository and ReportService with tests

**Files:**
- Create: `src/main/java/agrilife/ReportRepository.java`
- Create: `src/main/java/agrilife/ReportService.java`
- Create: `src/test/java/agrilife/ReportServiceTest.java`

- [ ] **Step 1: Write failing tests first**

Create `/Users/abhoumic/Downloads/AgriLife/src/test/java/agrilife/ReportServiceTest.java`:

```java
package agrilife;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ReportServiceTest {

    @Autowired
    private ReportRepository repository;

    private ReportService service;

    @BeforeEach
    void setUp() {
        service = new ReportService(repository);
    }

    private FarmerForm makeForm(String name, String crop, String pest) {
        FarmerForm f = new FarmerForm();
        f.setName(name);
        f.setPhone("9876543210");
        f.setCropName(crop);
        f.setPestName(pest);
        f.setDistance(20.0);
        f.setSpeed(40.0);
        return f;
    }

    @Test
    void save_persists_one_record() {
        service.save(makeForm("Rajesh", "Rice", "brown planthopper"), "Buprofezin", 35.0, 40.0);
        assertEquals(1, service.getAllReports().size());
    }

    @Test
    void save_stores_all_fields_correctly() {
        FarmerForm form = makeForm("Priya", "Wheat", "aphids");
        service.save(form, "Imidacloprid", 22.5, 50.0);
        ReportRecord saved = service.getAllReports().get(0);
        assertEquals("Priya", saved.getFarmerName());
        assertEquals("9876543210", saved.getPhone());
        assertEquals("Wheat", saved.getCropName());
        assertEquals("aphids", saved.getPestName());
        assertEquals("Imidacloprid", saved.getPesticide());
        assertEquals(22.5, saved.getDeliveryTime());
        assertEquals(20.0, saved.getDistance());
        assertEquals(50.0, saved.getSpeed());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void getAllReports_returns_newest_first() throws InterruptedException {
        service.save(makeForm("First", "Rice", "aphids"), "Imidacloprid", 20.0, 60.0);
        Thread.sleep(20);
        service.save(makeForm("Second", "Cotton", "bollworm"), "Emamectin Benzoate", 30.0, 60.0);
        List<ReportRecord> reports = service.getAllReports();
        assertEquals("Second", reports.get(0).getFarmerName());
        assertEquals("First", reports.get(1).getFarmerName());
    }

    @Test
    void empty_db_returns_empty_list() {
        assertTrue(service.getAllReports().isEmpty());
    }
}
```

- [ ] **Step 2: Run tests — verify they fail**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test -Dtest=ReportServiceTest 2>&1 | tail -10
```

Expected: compilation error — `ReportRepository` and `ReportService` do not exist yet.

- [ ] **Step 3: Create ReportRepository.java**

Create `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/ReportRepository.java`:

```java
package agrilife;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ReportRecord, Long> {
    List<ReportRecord> findAllByOrderByCreatedAtDesc();
}
```

- [ ] **Step 4: Create ReportService.java**

Create `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/ReportService.java`:

```java
package agrilife;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReportService {

    private final ReportRepository repository;

    public ReportService(ReportRepository repository) {
        this.repository = repository;
    }

    public void save(FarmerForm form, String pesticide, double deliveryTime, double effectiveSpeed) {
        ReportRecord record = new ReportRecord(
            form.getName(), form.getPhone(), form.getCropName(), form.getPestName(),
            pesticide, deliveryTime, form.getDistance(), effectiveSpeed
        );
        repository.save(record);
    }

    public List<ReportRecord> getAllReports() {
        return repository.findAllByOrderByCreatedAtDesc();
    }
}
```

- [ ] **Step 5: Run tests — verify 4 pass**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test -Dtest=ReportServiceTest 2>&1 | tail -10
```

Expected: `Tests run: 4, Failures: 0, Errors: 0` and `BUILD SUCCESS`

- [ ] **Step 6: Run full suite — verify existing 28 tests still pass**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test 2>&1 | grep -E "Tests run:|BUILD"
```

Expected: all tests pass, `BUILD SUCCESS`

- [ ] **Step 7: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/java/agrilife/ReportRepository.java \
        src/main/java/agrilife/ReportService.java \
        src/test/java/agrilife/ReportServiceTest.java
git commit -m "feat: add ReportRepository, ReportService, and persistence tests"
```

---

## Task 4: Wire ReportService into AgriLifeController

**Files:**
- Modify: `src/main/java/agrilife/AgriLifeController.java`

- [ ] **Step 1: Update AgriLifeController.java**

Replace `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/AgriLifeController.java` with:

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
    private final CropService cropService;
    private final ReportService reportService;

    public AgriLifeController(PestService pestService, DeliveryService deliveryService,
                               CropService cropService, ReportService reportService) {
        this.pestService = pestService;
        this.deliveryService = deliveryService;
        this.cropService = cropService;
        this.reportService = reportService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("farmerForm", new FarmerForm());
        model.addAttribute("crops", cropService.getAllCrops());
        model.addAttribute("cropPestMap", cropService.getCropPestMap());
        model.addAttribute("pestPhotoMap", pestService.getPestPhotoMap());
        return "index";
    }

    @PostMapping("/recommend")
    public String recommend(@ModelAttribute FarmerForm form, Model model) {
        Farmer farmer = new Farmer(form.getName(), form.getPhone(), form.getPestName());
        String recommendation = pestService.recommendPesticide(farmer.getPestName());
        String pestPhotoUrl = pestService.getPhotoUrl(farmer.getPestName());

        double deliveryTime;
        double effectiveSpeed = form.getSpeed();
        String deliveryError = null;
        try {
            deliveryTime = deliveryService.calculateDeliveryTime(form.getDistance(), form.getSpeed());
        } catch (IllegalArgumentException e) {
            effectiveSpeed = 30;
            deliveryTime = deliveryService.calculateDeliveryTime(form.getDistance(), effectiveSpeed);
            deliveryError = "Invalid speed — defaulted to 30 km/h.";
        }

        reportService.save(form, recommendation, deliveryTime, effectiveSpeed);

        model.addAttribute("farmer", farmer);
        model.addAttribute("cropName", form.getCropName());
        model.addAttribute("recommendation", recommendation);
        model.addAttribute("pestPhotoUrl", pestPhotoUrl);
        model.addAttribute("deliveryTime", String.format("%.1f", deliveryTime));
        model.addAttribute("distance", form.getDistance());
        model.addAttribute("speed", effectiveSpeed);
        model.addAttribute("deliveryError", deliveryError);
        return "result";
    }
}
```

- [ ] **Step 2: Run full test suite**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test 2>&1 | grep -E "Tests run:|BUILD"
```

Expected: all tests pass, `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/java/agrilife/AgriLifeController.java
git commit -m "feat: wire ReportService into AgriLifeController to persist submissions"
```

---

## Task 5: Create ReportsController

**Files:**
- Create: `src/main/java/agrilife/ReportsController.java`

- [ ] **Step 1: Create ReportsController.java**

Create `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/ReportsController.java`:

```java
package agrilife;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportsController {

    private final ReportService reportService;

    public ReportsController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("reports", reportService.getAllReports());
        return "reports";
    }
}
```

- [ ] **Step 2: Verify compile**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn compile -q 2>&1 | tail -3
```

Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/java/agrilife/ReportsController.java
git commit -m "feat: add ReportsController for GET /reports"
```

---

## Task 6: Add CSS for report table, accordion, and report history link

**Files:**
- Modify: `src/main/resources/static/style.css`

Run all 5 design audits from `src/DESIGN.md` before finalizing.

- [ ] **Step 1: Append report CSS to style.css**

Append to the end of `/Users/abhoumic/Downloads/AgriLife/src/main/resources/static/style.css`:

```css
/* ── Report History Table ── */
.report-title-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.report-count {
    font-size: 0.78rem;
    color: #9a9080;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.4px;
}

.report-table {
    width: 100%;
    border: 1px solid #e0d8cc;
    border-radius: 10px;
    overflow: hidden;
    margin-bottom: 24px;
}

.report-header-row {
    display: grid;
    grid-template-columns: 1.5fr 0.8fr 1.2fr 1.2fr 0.7fr 0.8fr;
    padding: 10px 16px;
    background: #f5f0e8;
    font-size: 0.72rem;
    font-weight: 700;
    color: #6b6b6b;
    text-transform: uppercase;
    letter-spacing: 0.4px;
}

.report-row {
    border-top: 1px solid #e8e0d0;
    cursor: pointer;
}

.report-row-summary {
    display: grid;
    grid-template-columns: 1.5fr 0.8fr 1.2fr 1.2fr 0.7fr 0.8fr;
    padding: 12px 16px;
    font-size: 0.875rem;
    align-items: center;
    background: #fff;
    transition: background-color 0.15s;
}

.report-row.even .report-row-summary {
    background: #fdfaf5;
}

.report-row:hover .report-row-summary {
    background: #fdf8f0;
}

.report-pesticide {
    color: #2d7a32;
    font-weight: 600;
}

.detail-panel {
    display: none;
    padding: 12px 16px 16px;
    background: #f9f6f0;
    border-top: 1px dashed #d0c9bb;
}

.detail-panel.expanded {
    display: block;
}

.detail-grid {
    display: grid;
    grid-template-columns: 120px 1fr;
    gap: 6px 16px;
    font-size: 0.85rem;
    color: #444;
}

.detail-grid .label {
    font-weight: 700;
    color: #6b6b6b;
    text-transform: uppercase;
    font-size: 0.72rem;
    letter-spacing: 0.4px;
    align-self: center;
}

/* ── Empty state ── */
.report-empty {
    padding: 40px 24px;
    text-align: center;
    color: #9a9080;
    font-size: 0.9rem;
    border: 1px solid #e0d8cc;
    border-radius: 10px;
    margin-bottom: 24px;
}

.report-empty a {
    color: #2d7a32;
    font-weight: 600;
    text-decoration: none;
}

.report-empty a:hover {
    text-decoration: underline;
}

/* ── Report history link (used on result.html) ── */
.report-history-link {
    display: block;
    text-align: center;
    margin-top: 12px;
    font-size: 0.875rem;
    color: #2d7a32;
    font-weight: 600;
    text-decoration: none;
}

.report-history-link:hover {
    text-decoration: underline;
}
```

- [ ] **Step 2: Run Design Audits 01–05**

01 HIERARCHY: report-title-row h2 = page title. report-count = secondary label (muted). Header row = column labels. Row data = content. Pesticide = green+bold = highlighted finding. Detail panel = subordinate context. Back link = exit. Correct hierarchy throughout.

02 TYPOGRAPHY: header-row 0.72rem 700 uppercase — matches existing `.label` pattern exactly. Row data 0.875rem — matches existing result-row. report-count 0.78rem uppercase — matches `.crop-badge` size. No new fonts or weights introduced.

03 WHITESPACE: row padding 12px 16px — slightly tighter than card padding (consistent with table density). Header 10px 16px — smaller to distinguish from data. Detail panel 12px 16px 16px — asymmetric bottom creates landing zone. report-empty 40px — generous centering for empty state.

04 COLOR: `.report-pesticide` uses #2d7a32 — same as `.btn-next` and `.highlight-box .value` green. Even row #fdfaf5 — same as `.crop-card` background. Hover #fdf8f0 — same warmth as card hover. Detail panel #f9f6f0 — one step warmer than page bg. No new colors.

05 CHEAP: (1) `overflow: hidden` on `.report-table` clips border-radius properly. (2) dashed border on detail panel signals "subordinate". (3) `cursor: pointer` on rows signals interactivity. (4) transition on row summary background = smooth hover. (5) `.report-pesticide` green matches result page — consistent visual language for the key finding.

Audits pass.

- [ ] **Step 3: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/resources/static/style.css
git commit -m "feat: add report table, accordion, and report history link CSS"
```

---

## Task 7: Create reports.html template

**Files:**
- Create: `src/main/resources/templates/reports.html`

Run all 5 design audits before finalizing.

- [ ] **Step 1: Create reports.html**

Create `/Users/abhoumic/Downloads/AgriLife/src/main/resources/templates/reports.html`:

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AgriLife — Report History</title>
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
                <h2>Report History</h2>
                <span class="report-count"
                      th:text="${reports.size()} + (${reports.size()} == 1 ? ' report' : ' reports')">0 reports</span>
            </div>

            <!-- Empty state -->
            <div th:if="${reports.isEmpty()}" class="report-empty">
                No reports yet.
                <a th:href="@{/}">Submit your first report →</a>
            </div>

            <!-- Report table -->
            <div th:if="${!reports.isEmpty()}" class="report-table">
                <div class="report-header-row">
                    <span>Farmer</span>
                    <span>Crop</span>
                    <span>Pest</span>
                    <span>Pesticide</span>
                    <span>Time</span>
                    <span>Date</span>
                </div>
                <div th:each="r, stat : ${reports}"
                     th:classappend="${stat.even} ? 'even' : ''"
                     class="report-row"
                     th:attr="onclick='toggleRow(' + ${r.id} + ')'">
                    <div class="report-row-summary">
                        <span th:text="${r.farmerName}">—</span>
                        <span th:text="${r.cropName}">—</span>
                        <span th:text="${r.pestName}">—</span>
                        <span class="report-pesticide" th:text="${r.pesticide}">—</span>
                        <span th:text="${#numbers.formatDecimal(r.deliveryTime, 1, 1)} + ' min'">—</span>
                        <span th:text="${#temporals.format(r.createdAt, 'dd MMM')}">—</span>
                    </div>
                    <div class="detail-panel" th:id="'detail-' + ${r.id}">
                        <div class="detail-grid">
                            <span class="label">Phone</span>
                            <span th:text="${r.phone}">—</span>
                            <span class="label">Distance</span>
                            <span th:text="${r.distance} + ' km'">—</span>
                            <span class="label">Speed</span>
                            <span th:text="${r.speed} + ' km/h'">—</span>
                            <span class="label">Submitted</span>
                            <span th:text="${#temporals.format(r.createdAt, 'dd MMM yyyy, HH:mm')}">—</span>
                        </div>
                    </div>
                </div>
            </div>

            <a th:href="@{/}" class="btn-secondary">← New Report</a>

        </div>
    </main>

    <footer>
        AgriLife Pest Management System &copy; 2026
    </footer>

    <script>
        function toggleRow(id) {
            const detail = document.getElementById('detail-' + id);
            detail.classList.toggle('expanded');
        }
    </script>
</body>
</html>
```

- [ ] **Step 2: Run Design Audits 01–05**

01 HIERARCHY: h2 title → muted count badge → table header (labels) → rows (data) → detail panel (subordinate). Back link is the only exit. No competing CTAs.

02 TYPOGRAPHY: All sizes from established scale. Header columns 0.72rem 700 uppercase matches label pattern. Row data 0.875rem matches result-row. report-pesticide green+600 weight = visual anchor per row. Date column `dd MMM` is compact and unambiguous.

03 WHITESPACE: Card's standard padding wraps the table cleanly. Table is flush to its border-radius container via `overflow: hidden`. Detail panel indented 16px matches row — reads as part of row, not new section.

04 COLOR: All colors from existing palette. th:classappend even/odd alternation uses established #fdfaf5. Hover uses #fdf8f0 — same warmth used on crop/pest card hovers. Detail panel dashed border in #d0c9bb matches step-line color.

05 CHEAP: (1) `toggleRow` is one line — no complexity. (2) `stat.even` for alternating rows is idiomatic Thymeleaf, not JS. (3) `#temporals.format` locale-formats the date — no raw timestamp output. (4) `report-empty` links directly to the wizard — no dead end. (5) Plural count `1 report` vs `2 reports` shows care.

Audits pass.

- [ ] **Step 3: Verify compile**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn compile -q 2>&1 | tail -3
```

Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/resources/templates/reports.html
git commit -m "feat: create reports.html with table, inline accordion, and empty state"
```

---

## Task 8: Add "View All Reports" link to result.html

**Files:**
- Modify: `src/main/resources/templates/result.html`

- [ ] **Step 1: Add report history link to result.html**

Read `/Users/abhoumic/Downloads/AgriLife/src/main/resources/templates/result.html` first.

Find this line:
```html
            <a th:href="@{/}" class="btn-secondary">← New Report</a>
```

Replace with:
```html
            <a th:href="@{/}" class="btn-secondary">← New Report</a>
            <a th:href="@{/reports}" class="report-history-link">View All Reports →</a>
```

- [ ] **Step 2: Verify compile**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn compile -q 2>&1 | tail -3
```

Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/resources/templates/result.html
git commit -m "feat: add View All Reports link to result page"
```

---

## Task 9: End-to-end verification

- [ ] **Step 1: Run full test suite**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test 2>&1 | grep -E "Tests run:|BUILD"
```

Expected: 32 tests (28 original + 4 new ReportServiceTest), 0 failures, `BUILD SUCCESS`

- [ ] **Step 2: Start app**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn spring-boot:run > /tmp/agrilife-p2.log 2>&1 &
sleep 15 && grep -E "Started|ERROR" /tmp/agrilife-p2.log | head -5
```

Expected: `Started AgriLifeApplication in X seconds`

- [ ] **Step 3: Verify /reports returns 200 with empty state**

```bash
curl -s http://localhost:8080/reports | grep -E "Report History|No reports yet|report-empty"
```

Expected: page contains "Report History" and "No reports yet"

- [ ] **Step 4: Submit a report and verify it is saved**

```bash
curl -s -X POST http://localhost:8080/recommend \
  -d "name=Rajesh+Kumar&phone=9876543210&cropName=Rice&pestName=brown+planthopper&distance=20&speed=40" \
  | grep -E "Buprofezin|View All Reports"
```

Expected: response contains "Buprofezin" and "View All Reports →"

- [ ] **Step 5: Verify report appears in history**

```bash
curl -s http://localhost:8080/reports | grep -E "Rajesh|Rice|Buprofezin|report-row"
```

Expected: farmer name, crop, and pesticide appear in the report table

- [ ] **Step 6: Submit a second report and verify ordering**

```bash
curl -s -X POST http://localhost:8080/recommend \
  -d "name=Priya+Sharma&phone=1234567890&cropName=Cotton&pestName=bollworm&distance=15&speed=60" \
  | grep "Emamectin"
```

Then:

```bash
curl -s http://localhost:8080/reports | grep -o 'farmerName\|Rajesh\|Priya' | head -5
```

Expected: "Priya" appears before "Rajesh" (newest first)

- [ ] **Step 7: Stop the app**

```bash
pkill -f "spring-boot:run" 2>/dev/null; pkill -f "AgriLifeApplication" 2>/dev/null; echo "stopped"
```

- [ ] **Step 8: Final commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git status
git add -A
git commit -m "feat: Phase 2 complete — H2 persistence, report history at /reports, inline accordion" 2>/dev/null || echo "nothing to commit"
```

---

## Self-Review

**Spec coverage:**
- ✅ H2 in-memory database → Task 1 (pom.xml + application.properties)
- ✅ ReportRecord entity — all 10 fields from spec → Task 2
- ✅ `findAllByOrderByCreatedAtDesc()` → Task 3 (ReportRepository)
- ✅ `save(form, pesticide, deliveryTime, effectiveSpeed)` → Task 3 (ReportService)
- ✅ 4 ReportServiceTest tests: save, all-fields, ordering, empty → Task 3
- ✅ `reportService.save()` called in POST /recommend → Task 4
- ✅ GET /reports → Task 5 (ReportsController)
- ✅ Report table: all 6 columns (farmer, crop, pest, pesticide, time, date) → Task 7
- ✅ Click row = `toggleRow(id)` → `.expanded` toggle → Task 7 (JS) + Task 6 (CSS)
- ✅ Empty state with link to / → Task 7
- ✅ Newest first sort → Task 3 (repository method) + Task 7 (th:each)
- ✅ Detail panel: phone, distance, speed, full timestamp → Task 7
- ✅ "View All Reports →" on result.html → Task 8
- ✅ Design audits on CSS + HTML → Tasks 6, 7
- ✅ Existing 28 tests still pass → Task 3 step 6, Task 9 step 1

**Placeholder scan:** No TBDs. All code is complete. `#temporals.format` pattern explicit. Grid column ratios explicit. Even/odd alternation uses Thymeleaf `stat.even` — no ambiguity.

**Type consistency:**
- `ReportService.save(FarmerForm, String, double, double)` matches call in `AgriLifeController`: `reportService.save(form, recommendation, deliveryTime, effectiveSpeed)` ✅
- `ReportService.getAllReports()` returns `List<ReportRecord>` → `model.addAttribute("reports", ...)` → `th:each="r : ${reports}"` → `r.farmerName`, `r.cropName`, etc. match entity getters ✅
- `toggleRow(id)` in JS matches `th:attr="onclick='toggleRow(' + ${r.id} + ')'"` → `document.getElementById('detail-' + id)` matches `th:id="'detail-' + ${r.id}"` ✅
- `stat.even` in Thymeleaf is a boolean — `th:classappend="${stat.even} ? 'even' : ''"` produces class `"report-row even"` or `"report-row"` ✅
