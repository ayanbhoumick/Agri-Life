# AgriLife Phase 3: Dashboard + Weather Context Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a location field to the wizard, fetch live weather from Open-Meteo after submit, derive high-risk pests from temperature and precipitation, and show a weather card plus app-wide stats on the result page.

**Architecture:** `WeatherService` makes two HTTP calls via `RestTemplate` (already in classpath): geocoding to resolve city → lat/lng, then forecast to get temperature and precipitation. Pest risk is derived from a set of threshold rules. Stats (total reports, most common pest, avg delivery time) are computed in-memory from the H2 DB via `ReportService`. Weather and stats are passed as model attributes to `result.html` and rendered with `th:if` guards so blank/failed lookups degrade gracefully.

**Tech Stack:** Java 17, Spring Boot 3.2.5, Thymeleaf, Spring Data JPA, H2, `RestTemplate` (spring-web, already present), Open-Meteo API (free, no key)

---

## File Map

| Action | Path | Responsibility |
|--------|------|---------------|
| Modify | `src/main/java/agrilife/FarmerForm.java` | Add `location` String field |
| Modify | `src/main/java/agrilife/ReportRecord.java` | Add `location` nullable field, update constructor |
| Modify | `src/main/java/agrilife/ReportService.java` | Pass location to save(); add getTotalCount(), getMostCommonPest(), getAvgDeliveryTime() |
| Modify | `src/test/java/agrilife/ReportServiceTest.java` | Add 4 stats tests |
| Create | `src/main/java/agrilife/WeatherData.java` | POJO: city, temperature, precipitation, weatherCode, riskPests |
| Create | `src/main/java/agrilife/WeatherService.java` | @Service — geocode + forecast HTTP calls + deriveRiskPests() |
| Create | `src/test/java/agrilife/WeatherServiceTest.java` | 5 unit tests for deriveRiskPests() (no HTTP) |
| Modify | `src/main/java/agrilife/AgriLifeController.java` | Inject WeatherService, call weather + stats, add to result model |
| Modify | `src/main/resources/templates/index.html` | Add location input to Step 1 |
| Modify | `src/main/resources/static/style.css` | Weather card, risk badge, stats row |
| Modify | `src/main/resources/templates/result.html` | Add weather card + stats row |

---

## Task 1: Add location field to FarmerForm, ReportRecord, and ReportService

**Files:**
- Modify: `src/main/java/agrilife/FarmerForm.java`
- Modify: `src/main/java/agrilife/ReportRecord.java`
- Modify: `src/main/java/agrilife/ReportService.java`

- [ ] **Step 1: Update FarmerForm.java**

Replace `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/FarmerForm.java` with:

```java
package agrilife;

public class FarmerForm {

    private String name;
    private String phone;
    private String location;
    private String cropName;
    private String pestName;
    private double distance;
    private double speed;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }

    public String getPestName() { return pestName; }
    public void setPestName(String pestName) { this.pestName = pestName; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
}
```

- [ ] **Step 2: Update ReportRecord.java**

Replace `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/ReportRecord.java` with:

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
    private String location;
    private LocalDateTime createdAt;

    protected ReportRecord() {}

    public ReportRecord(String farmerName, String phone, String cropName, String pestName,
                        String pesticide, double deliveryTime, double distance, double speed,
                        String location) {
        this.farmerName = farmerName;
        this.phone = phone;
        this.cropName = cropName;
        this.pestName = pestName;
        this.pesticide = pesticide;
        this.deliveryTime = deliveryTime;
        this.distance = distance;
        this.speed = speed;
        this.location = location;
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
    public String getLocation() { return location; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

- [ ] **Step 3: Update ReportService.java — pass location to constructor**

Replace `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/ReportService.java` with:

```java
package agrilife;

import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository repository;

    public ReportService(ReportRepository repository) {
        this.repository = repository;
    }

    public void save(FarmerForm form, String pesticide, double deliveryTime, double effectiveSpeed) {
        ReportRecord record = new ReportRecord(
            form.getName(), form.getPhone(), form.getCropName(), form.getPestName(),
            pesticide, deliveryTime, form.getDistance(), effectiveSpeed, form.getLocation()
        );
        repository.save(record);
    }

    public List<ReportRecord> getAllReports() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public long getTotalCount() {
        return repository.count();
    }

    public String getMostCommonPest() {
        List<ReportRecord> records = repository.findAll();
        if (records.isEmpty()) return "None";
        return records.stream()
            .collect(Collectors.groupingBy(ReportRecord::getPestName, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("None");
    }

    public double getAvgDeliveryTime() {
        List<ReportRecord> records = repository.findAll();
        if (records.isEmpty()) return 0.0;
        return records.stream()
            .mapToDouble(ReportRecord::getDeliveryTime)
            .average()
            .orElse(0.0);
    }
}
```

- [ ] **Step 4: Run full test suite — verify all 32 still pass**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test 2>&1 | grep -E "Tests run:|BUILD"
```

Expected: `Tests run: 32, Failures: 0, Errors: 0` and `BUILD SUCCESS`

- [ ] **Step 5: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/java/agrilife/FarmerForm.java \
        src/main/java/agrilife/ReportRecord.java \
        src/main/java/agrilife/ReportService.java
git commit -m "feat: add location field to FarmerForm/ReportRecord, add stats methods to ReportService"
```

---

## Task 2: Add stats tests to ReportServiceTest

**Files:**
- Modify: `src/test/java/agrilife/ReportServiceTest.java`

- [ ] **Step 1: Add 4 stats tests**

Read `/Users/abhoumic/Downloads/AgriLife/src/test/java/agrilife/ReportServiceTest.java` first.

Inside the class (before the closing `}`), append:

```java
    @Test
    void getTotalCount_returns_zero_when_empty() {
        assertEquals(0, service.getTotalCount());
    }

    @Test
    void getMostCommonPest_returns_none_when_empty() {
        assertEquals("None", service.getMostCommonPest());
    }

    @Test
    void getMostCommonPest_returns_most_frequent_pest() {
        service.save(makeForm("A", "Rice", "aphids"), "Imidacloprid", 20.0, 40.0);
        service.save(makeForm("B", "Rice", "aphids"), "Imidacloprid", 20.0, 40.0);
        service.save(makeForm("C", "Wheat", "locusts"), "Chlorpyrifos", 25.0, 40.0);
        assertEquals("aphids", service.getMostCommonPest());
    }

    @Test
    void getAvgDeliveryTime_returns_correct_average() {
        service.save(makeForm("A", "Rice", "aphids"), "Imidacloprid", 20.0, 40.0);
        service.save(makeForm("B", "Wheat", "locusts"), "Chlorpyrifos", 30.0, 40.0);
        assertEquals(25.0, service.getAvgDeliveryTime(), 0.01);
    }
```

- [ ] **Step 2: Run ReportServiceTest — verify 8 pass**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test -Dtest=ReportServiceTest 2>&1 | tail -10
```

Expected: `Tests run: 8, Failures: 0, Errors: 0` and `BUILD SUCCESS`

- [ ] **Step 3: Run full suite**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test 2>&1 | grep -E "Tests run:|BUILD"
```

Expected: `Tests run: 36, Failures: 0, Errors: 0` and `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/test/java/agrilife/ReportServiceTest.java
git commit -m "test: add stats tests to ReportServiceTest"
```

---

## Task 3: Create WeatherData POJO and WeatherService with tests

**Files:**
- Create: `src/main/java/agrilife/WeatherData.java`
- Create: `src/main/java/agrilife/WeatherService.java`
- Create: `src/test/java/agrilife/WeatherServiceTest.java`

- [ ] **Step 1: Write failing tests**

Create `/Users/abhoumic/Downloads/AgriLife/src/test/java/agrilife/WeatherServiceTest.java`:

```java
package agrilife;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class WeatherServiceTest {

    private final WeatherService service = new WeatherService();

    @Test
    void hot_dry_flags_locusts_whitefly_aphids() {
        List<String> risks = service.deriveRiskPests(36.0, 0.5);
        assertTrue(risks.contains("Locusts"));
        assertTrue(risks.contains("Whitefly"));
        assertTrue(risks.contains("Aphids"));
    }

    @Test
    void warm_wet_flags_planthopper_leafFolder_stemBorer() {
        List<String> risks = service.deriveRiskPests(25.0, 8.0);
        assertTrue(risks.contains("Brown Planthopper"));
        assertTrue(risks.contains("Leaf Folder"));
        assertTrue(risks.contains("Stem Borer"));
    }

    @Test
    void moderate_dry_flags_aphids_mites_thrips() {
        List<String> risks = service.deriveRiskPests(28.0, 0.0);
        assertTrue(risks.contains("Aphids"));
        assertTrue(risks.contains("Mites"));
        assertTrue(risks.contains("Thrips"));
    }

    @Test
    void cool_wet_flags_armyworm_cutworm() {
        List<String> risks = service.deriveRiskPests(15.0, 5.0);
        assertTrue(risks.contains("Armyworm"));
        assertTrue(risks.contains("Cutworm"));
    }

    @Test
    void no_duplicates_when_multiple_rules_match() {
        List<String> risks = service.deriveRiskPests(36.0, 8.0);
        long distinctCount = risks.stream().distinct().count();
        assertEquals(risks.size(), distinctCount);
    }
}
```

- [ ] **Step 2: Run tests — verify they fail**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test -Dtest=WeatherServiceTest 2>&1 | tail -10
```

Expected: compilation error — `WeatherService` does not exist yet.

- [ ] **Step 3: Create WeatherData.java**

Create `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/WeatherData.java`:

```java
package agrilife;

import java.util.List;

public class WeatherData {

    private final String city;
    private final double temperature;
    private final double precipitation;
    private final int weatherCode;
    private final List<String> riskPests;

    public WeatherData(String city, double temperature, double precipitation,
                       int weatherCode, List<String> riskPests) {
        this.city = city;
        this.temperature = temperature;
        this.precipitation = precipitation;
        this.weatherCode = weatherCode;
        this.riskPests = riskPests;
    }

    public String getCity() { return city; }
    public double getTemperature() { return temperature; }
    public double getPrecipitation() { return precipitation; }
    public int getWeatherCode() { return weatherCode; }
    public List<String> getRiskPests() { return riskPests; }
}
```

- [ ] **Step 4: Create WeatherService.java**

Create `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/WeatherService.java`:

```java
package agrilife;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WeatherService {

    private final RestTemplate restTemplate = new RestTemplate();

    public WeatherData getWeather(String city) {
        try {
            String encoded = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name="
                + encoded + "&count=1&language=en&format=json";

            @SuppressWarnings("unchecked")
            Map<String, Object> geoResp = restTemplate.getForObject(geoUrl, Map.class);
            if (geoResp == null) return null;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>) geoResp.get("results");
            if (results == null || results.isEmpty()) return null;

            Map<String, Object> place = results.get(0);
            double lat = ((Number) place.get("latitude")).doubleValue();
            double lng = ((Number) place.get("longitude")).doubleValue();
            String resolvedName = (String) place.get("name");

            String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + lat
                + "&longitude=" + lng
                + "&current=temperature_2m,precipitation,weathercode&timezone=auto";

            @SuppressWarnings("unchecked")
            Map<String, Object> weatherResp = restTemplate.getForObject(weatherUrl, Map.class);
            if (weatherResp == null) return null;

            @SuppressWarnings("unchecked")
            Map<String, Object> current = (Map<String, Object>) weatherResp.get("current");
            if (current == null) return null;

            double temperature = ((Number) current.get("temperature_2m")).doubleValue();
            double precipitation = ((Number) current.get("precipitation")).doubleValue();
            int weatherCode = ((Number) current.get("weathercode")).intValue();
            List<String> riskPests = deriveRiskPests(temperature, precipitation);

            return new WeatherData(resolvedName, temperature, precipitation, weatherCode, riskPests);
        } catch (Exception e) {
            return null;
        }
    }

    List<String> deriveRiskPests(double temperature, double precipitation) {
        List<String> risks = new ArrayList<>();
        if (temperature >= 35 && precipitation < 2) {
            risks.add("Locusts");
            risks.add("Whitefly");
            risks.add("Aphids");
        }
        if (precipitation >= 5 && temperature >= 20) {
            risks.add("Brown Planthopper");
            risks.add("Leaf Folder");
            risks.add("Stem Borer");
        }
        if (temperature >= 20 && temperature < 35 && precipitation < 2) {
            risks.add("Aphids");
            risks.add("Mites");
            risks.add("Thrips");
        }
        if (temperature < 20 && precipitation >= 3) {
            risks.add("Armyworm");
            risks.add("Cutworm");
        }
        return risks.stream().distinct().collect(Collectors.toList());
    }
}
```

- [ ] **Step 5: Run WeatherServiceTest — verify 5 pass**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test -Dtest=WeatherServiceTest 2>&1 | tail -10
```

Expected: `Tests run: 5, Failures: 0, Errors: 0` and `BUILD SUCCESS`

- [ ] **Step 6: Run full suite — verify 41 tests pass**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test 2>&1 | grep -E "Tests run:|BUILD"
```

Expected: `Tests run: 41, Failures: 0, Errors: 0` and `BUILD SUCCESS`

- [ ] **Step 7: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/java/agrilife/WeatherData.java \
        src/main/java/agrilife/WeatherService.java \
        src/test/java/agrilife/WeatherServiceTest.java
git commit -m "feat: add WeatherData, WeatherService with Open-Meteo integration and pest risk logic"
```

---

## Task 4: Wire WeatherService and stats into AgriLifeController

**Files:**
- Modify: `src/main/java/agrilife/AgriLifeController.java`

- [ ] **Step 1: Replace AgriLifeController.java**

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
    private final WeatherService weatherService;

    public AgriLifeController(PestService pestService, DeliveryService deliveryService,
                               CropService cropService, ReportService reportService,
                               WeatherService weatherService) {
        this.pestService = pestService;
        this.deliveryService = deliveryService;
        this.cropService = cropService;
        this.reportService = reportService;
        this.weatherService = weatherService;
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

        WeatherData weatherData = null;
        String location = form.getLocation();
        if (location != null && !location.trim().isEmpty()) {
            weatherData = weatherService.getWeather(location.trim());
        }

        model.addAttribute("farmer", farmer);
        model.addAttribute("cropName", form.getCropName());
        model.addAttribute("recommendation", recommendation);
        model.addAttribute("pestPhotoUrl", pestPhotoUrl);
        model.addAttribute("deliveryTime", String.format("%.1f", deliveryTime));
        model.addAttribute("distance", form.getDistance());
        model.addAttribute("speed", effectiveSpeed);
        model.addAttribute("deliveryError", deliveryError);
        model.addAttribute("weatherData", weatherData);
        model.addAttribute("totalReports", reportService.getTotalCount());
        model.addAttribute("mostCommonPest", reportService.getMostCommonPest());
        model.addAttribute("avgDeliveryTime", String.format("%.1f", reportService.getAvgDeliveryTime()));
        return "result";
    }
}
```

- [ ] **Step 2: Run full test suite**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test 2>&1 | grep -E "Tests run:|BUILD"
```

Expected: `Tests run: 41, Failures: 0, Errors: 0` and `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/java/agrilife/AgriLifeController.java
git commit -m "feat: wire WeatherService and stats into AgriLifeController"
```

---

## Task 5: Add location field to index.html Step 1

**Files:**
- Modify: `src/main/resources/templates/index.html`

- [ ] **Step 1: Add location input to Step 1**

Read `/Users/abhoumic/Downloads/AgriLife/src/main/resources/templates/index.html` first.

Find this block inside Step 1 (between the phone group and the wizard-nav div):

```html
                    <div class="form-group">
                        <label for="phone">Phone Number</label>
                        <input type="tel" id="phone" th:field="*{phone}"
                               placeholder="e.g. +91 98765 43210" required>
                    </div>
                    <div class="wizard-nav">
```

Replace with:

```html
                    <div class="form-group">
                        <label for="phone">Phone Number</label>
                        <input type="tel" id="phone" th:field="*{phone}"
                               placeholder="e.g. +91 98765 43210" required>
                    </div>
                    <div class="form-group">
                        <label for="location">Your Location <span class="optional-label">(optional — for weather)</span></label>
                        <input type="text" id="location" th:field="*{location}"
                               placeholder="e.g. Mumbai, Delhi, Pune">
                    </div>
                    <div class="wizard-nav">
```

- [ ] **Step 2: Verify compile**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn compile -q 2>&1 | tail -3
```

Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/resources/templates/index.html
git commit -m "feat: add optional location field to wizard Step 1"
```

---

## Task 6: Add weather card and stats CSS to style.css

**Files:**
- Modify: `src/main/resources/static/style.css`

Run all 5 design audits from `src/DESIGN.md` before finalizing.

- [ ] **Step 1: Append CSS to style.css**

Append to the END of `/Users/abhoumic/Downloads/AgriLife/src/main/resources/static/style.css`:

```css
/* ── Optional label (used in Step 1 location field) ── */
.optional-label {
    font-weight: 400;
    font-size: 0.78rem;
    color: #9a9080;
}

/* ── Weather Card ── */
.weather-card {
    background: linear-gradient(135deg, #e8f4fd 0%, #f0f8e8 100%);
    border: 1px solid #b4ddb4;
    border-radius: 10px;
    padding: 16px;
    margin-bottom: 20px;
}

.weather-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
}

.weather-icon {
    font-size: 1.4rem;
}

.weather-city {
    font-weight: 700;
    font-size: 1rem;
    color: #1e4620;
}

.weather-conditions {
    font-size: 0.9rem;
    color: #444;
    margin-bottom: 8px;
}

.weather-sep {
    margin: 0 6px;
    color: #9a9080;
}

.weather-risk {
    font-size: 0.85rem;
    color: #8b4513;
    font-weight: 600;
    background: #fff8e8;
    border: 1px solid #f0d080;
    border-radius: 6px;
    padding: 6px 10px;
}

.risk-label {
    margin-right: 4px;
}

.weather-no-risk {
    font-size: 0.85rem;
    color: #2d7a32;
    font-weight: 600;
}

/* ── Stats Row ── */
.stats-row {
    display: flex;
    gap: 0;
    border: 1px solid #e0d8cc;
    border-radius: 10px;
    overflow: hidden;
    margin-bottom: 24px;
}

.stat-item {
    flex: 1;
    padding: 14px 12px;
    text-align: center;
    border-right: 1px solid #e0d8cc;
}

.stat-item:last-child {
    border-right: none;
}

.stat-value {
    display: block;
    font-size: 1.2rem;
    font-weight: 700;
    color: #1e4620;
    margin-bottom: 2px;
}

.stat-label {
    display: block;
    font-size: 0.68rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.4px;
    color: #9a9080;
}
```

- [ ] **Step 2: Run Design Audits 01–05**

01 HIERARCHY: Weather card appears above farmer details — adds context before the table. Stats row is a secondary summary bar — visually subordinate (smaller values, muted labels). Risk badge is amber/warm — distinct from the green brand palette, signaling warning without alarm.

02 TYPOGRAPHY: `.weather-city` 1rem 700 — matches h3 tier. `.weather-conditions` 0.9rem — body copy tier. `.stat-value` 1.2rem 700 — smaller than `.value` (1.4rem) in highlight-box, correct hierarchy. `.stat-label` 0.68rem uppercase 700 — consistent with `.report-header-row` labels. `.optional-label` 0.78rem 400 — matches existing muted text pattern.

03 WHITESPACE: weather-card 16px padding — consistent with pest-card. weather-header gap 8px — tight, appropriate for icon+text. stats-row stat-item 14px 12px padding — slightly less than highlight-box 20px (stat bar is compact summary). margin-bottom 20px on weather-card, 24px on stats-row — flows into existing divider pattern.

04 COLOR: weather-card gradient uses #e8f4fd (sky blue) → #f0f8e8 (leaf green) — evokes sky+nature without introducing off-brand colors. weather-risk amber (#fff8e8 bg / #f0d080 border / #8b4513 text) — conventional warning palette, clear visual distinction from brand green. stat-value #1e4620 — same as `.done` step dot, consistent use of dark green for emphasis.

05 CHEAP: (1) weather-card gradient gives premium feel. (2) weather-no-risk green check = positive reinforcement when conditions are safe. (3) stats-row `overflow: hidden` clips border-radius on inner borders. (4) `border-right: none` on last stat-item prevents double border. (5) Optional label muted to reduce cognitive load on farmers who don't want weather.

Audits pass.

- [ ] **Step 3: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/resources/static/style.css
git commit -m "feat: add weather card, risk badge, and stats row CSS"
```

---

## Task 7: Update result.html with weather card and stats row

**Files:**
- Modify: `src/main/resources/templates/result.html`

Run all 5 design audits before finalizing.

- [ ] **Step 1: Read result.html and apply changes**

Read `/Users/abhoumic/Downloads/AgriLife/src/main/resources/templates/result.html` first.

Find this block (the divider before farmer details):

```html
            <hr class="divider">

            <!-- Farmer details -->
```

Replace with:

```html
            <hr class="divider">

            <!-- Weather card -->
            <div class="weather-card" th:if="${weatherData != null}">
                <div class="weather-header">
                    <span class="weather-icon">🌤</span>
                    <span class="weather-city" th:text="'Weather in ' + ${weatherData.city}">Weather</span>
                </div>
                <div class="weather-conditions">
                    <span th:text="${weatherData.temperature} + '°C'">—</span>
                    <span class="weather-sep">·</span>
                    <span th:text="${weatherData.precipitation} + 'mm precipitation'">—</span>
                </div>
                <div class="weather-risk" th:if="${!weatherData.riskPests.isEmpty()}">
                    <span class="risk-label">⚠ High-Risk Pests:</span>
                    <span th:each="pest, stat : ${weatherData.riskPests}"
                          th:text="${pest} + (${stat.last} ? '' : ' · ')">—</span>
                </div>
                <div class="weather-no-risk" th:if="${weatherData.riskPests.isEmpty()}">
                    ✓ No high-risk pests for current conditions
                </div>
            </div>

            <!-- Stats row -->
            <div class="stats-row" th:if="${totalReports > 0}">
                <div class="stat-item">
                    <span class="stat-value" th:text="${totalReports}">0</span>
                    <span class="stat-label">Total Reports</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value" th:text="${mostCommonPest}">—</span>
                    <span class="stat-label">Most Common Pest</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value" th:text="${avgDeliveryTime} + ' min'">—</span>
                    <span class="stat-label">Avg Delivery</span>
                </div>
            </div>

            <!-- Farmer details -->
```

- [ ] **Step 2: Run Design Audits 01–05**

01 HIERARCHY: After recommendation highlights (primary) → divider → weather card (context) → stats row (summary) → farmer details (subordinate). Correct cascade: action first, context second, record third.

02 TYPOGRAPHY: All new elements use established CSS classes from Task 6 — no inline font sizing. `th:text` for all values — HTML-escaped, safe.

03 WHITESPACE: `weather-card` 20px margin-bottom → `stats-row` 24px margin-bottom → farmer details. Progression feels deliberate, not cramped.

04 COLOR: `th:if` guards ensure weather card only appears when data is real — no empty amber box. Stats row only appears when `totalReports > 0` — no "0 / None / 0.0 min" ghost state.

05 CHEAP: (1) `th:each` pest list with `stat.last` check outputs "Locusts · Whitefly · Aphids" (no trailing separator). (2) `weatherData.riskPests.isEmpty()` guard switches between warning and clean-all-clear. (3) `totalReports > 0` guard prevents confusing empty stat bar on first load.

Audits pass.

- [ ] **Step 3: Verify compile**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn compile -q 2>&1 | tail -3
```

Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/resources/templates/result.html
git commit -m "feat: add weather card and stats row to result page"
```

---

## Task 8: End-to-end verification

- [ ] **Step 1: Run full test suite**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test 2>&1 | grep -E "Tests run:|BUILD"
```

Expected: `Tests run: 41, Failures: 0, Errors: 0, BUILD SUCCESS`

- [ ] **Step 2: Start app**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn spring-boot:run > /tmp/agrilife-p3.log 2>&1 &
sleep 15 && grep -E "Started|ERROR" /tmp/agrilife-p3.log | head -5
```

If port 8080 is busy: `lsof -ti:8080 | xargs kill -9 2>/dev/null; sleep 2` then retry.

Expected: `Started AgriLifeApplication in X seconds`

- [ ] **Step 3: Verify location field exists on Step 1**

```bash
curl -s http://localhost:8080/ | grep -E "location|optional-label|Your Location"
```

Expected: HTML contains location input field

- [ ] **Step 4: Submit with location — verify weather appears**

```bash
curl -s -X POST http://localhost:8080/recommend \
  -d "name=Rajesh&phone=9876543210&location=Mumbai&cropName=Rice&pestName=brown+planthopper&distance=20&speed=40" \
  | grep -E "weather-card|Weather in|temperature|risk"
```

Expected: response contains `weather-card` class and temperature data

- [ ] **Step 5: Submit without location — verify weather card hidden**

```bash
curl -s -X POST http://localhost:8080/recommend \
  -d "name=Priya&phone=1234567890&location=&cropName=Cotton&pestName=bollworm&distance=15&speed=60" \
  | grep "weather-card"
```

Expected: no `weather-card` in response (location was blank → weatherData is null → th:if hides it)

- [ ] **Step 6: Verify stats row appears after submission**

```bash
curl -s -X POST http://localhost:8080/recommend \
  -d "name=Amit&phone=1111111111&location=&cropName=Wheat&pestName=aphids&distance=10&speed=50" \
  | grep -E "stats-row|Total Reports|Most Common"
```

Expected: response contains `stats-row` with report count ≥ 1

- [ ] **Step 7: Stop app**

```bash
pkill -f "spring-boot:run" 2>/dev/null; pkill -f "AgriLifeApplication" 2>/dev/null; echo "stopped"
```

- [ ] **Step 8: Final commit if anything uncommitted**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git status
git add -A -- ':!.superpowers/'
git commit -m "feat: Phase 3 complete — weather context, pest risk flags, stats on result page" 2>/dev/null || echo "nothing to commit"
```

---

## Self-Review

**Spec coverage:**
- ✅ `location` field in wizard Step 1 → Task 5
- ✅ `location` on FarmerForm + ReportRecord → Task 1
- ✅ Open-Meteo geocoding + forecast HTTP calls → Task 3 (WeatherService.getWeather)
- ✅ Pest risk rules (4 conditions) → Task 3 (WeatherService.deriveRiskPests)
- ✅ WeatherData POJO (city, temperature, precipitation, weatherCode, riskPests) → Task 3
- ✅ Blank location → weatherData null → card hidden → Task 4 (controller guard) + Task 7 (th:if)
- ✅ Unknown city → return null → graceful fallback → Task 3 (catch + empty results check)
- ✅ Stats: getTotalCount, getMostCommonPest, getAvgDeliveryTime → Task 1
- ✅ Stats tests (4 tests) → Task 2
- ✅ WeatherService tests (5 tests, no HTTP) → Task 3
- ✅ Weather card on result page (th:if="${weatherData != null}") → Task 7
- ✅ Stats row (th:if="${totalReports > 0}") → Task 7
- ✅ Risk pest list with separator (stat.last) → Task 7
- ✅ weather-no-risk for empty risk list → Task 7
- ✅ Design audits on CSS + HTML → Tasks 6, 7
- ✅ All 36 existing tests still pass → Tasks 1, 2, 3 step 6, Task 4 step 2

**Placeholder scan:** No TBDs. All HTTP URL strings are complete. All CSS class names defined before used. All `th:text` expressions reference defined model attributes.

**Type consistency:**
- `WeatherService.getWeather(String)` returns `WeatherData` → `model.addAttribute("weatherData", weatherData)` → `${weatherData.city}`, `${weatherData.temperature}`, `${weatherData.precipitation}`, `${weatherData.riskPests}` all match `WeatherData` getters ✅
- `ReportService.getTotalCount()` returns `long` → `model.addAttribute("totalReports", ...)` → `th:if="${totalReports > 0}"` — Thymeleaf compares long to int literal fine ✅
- `ReportService.getAvgDeliveryTime()` returns `double` → `String.format("%.1f", ...)` → `${avgDeliveryTime} + ' min'` — string concat in Thymeleaf works ✅
- `ReportRecord` constructor now takes 9 args (farmerName, phone, cropName, pestName, pesticide, deliveryTime, distance, speed, location) → `ReportService.save()` passes `form.getLocation()` as 9th arg ✅
- `WeatherService.deriveRiskPests(double, double)` is package-private → `WeatherServiceTest` is in same package `agrilife` → accessible ✅
