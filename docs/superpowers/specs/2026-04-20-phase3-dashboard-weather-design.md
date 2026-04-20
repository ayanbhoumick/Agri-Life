# AgriLife Phase 3: Dashboard + Weather Context Design

**Date:** 2026-04-20

## Goal

Show weather context and submission stats on the result page. User types a location in Step 1 of the wizard; after submit, the result page shows current weather for that city, high-risk pests based on conditions, and app-wide stats (total reports, most common pest, avg delivery time).

## Tech Stack

Existing: Java 17, Spring Boot 3.2.5, Thymeleaf, Spring Data JPA, H2  
New: Open-Meteo geocoding API + forecast API (free, no key), Spring `RestTemplate` (already in spring-boot-starter-web)

## Architecture

`WeatherService` makes two sequential HTTP GET calls via `RestTemplate`:
1. Geocoding: `https://geocoding-api.open-meteo.com/v1/search?name={city}&count=1` → lat/lng
2. Forecast: `https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lng}&current=temperature_2m,precipitation,weathercode&timezone=auto` → temp + precipitation

If either call fails (network error, unknown city, empty results), `WeatherService.getWeather()` returns `null`. Controller checks for null and skips the weather section in the model. Thymeleaf `th:if` guards prevent empty elements.

Stats computed in-memory from all `ReportRecord` rows via `ReportService` — no new DB queries needed for small datasets.

`RestTemplate` instantiated directly in `WeatherService` constructor (no bean required).

## File Changes

| Action | Path | Responsibility |
|--------|------|---------------|
| Create | `src/main/java/agrilife/WeatherData.java` | POJO: city, temperature, precipitation, weatherCode, riskPests |
| Create | `src/main/java/agrilife/WeatherService.java` | @Service — geocode + fetch weather + derive risk pests |
| Create | `src/test/java/agrilife/WeatherServiceTest.java` | Unit tests for pest risk logic (no HTTP) |
| Modify | `src/main/java/agrilife/FarmerForm.java` | Add `location` String field |
| Modify | `src/main/java/agrilife/ReportRecord.java` | Add `location` String field (nullable) |
| Modify | `src/main/java/agrilife/ReportService.java` | Add `getTotalCount()`, `getMostCommonPest()`, `getAvgDeliveryTime()` |
| Modify | `src/main/java/agrilife/AgriLifeController.java` | Call WeatherService + ReportService stats, pass to result model |
| Modify | `src/main/resources/templates/index.html` | Add location field to Step 1 |
| Modify | `src/main/resources/templates/result.html` | Add weather card + stats row at bottom |
| Modify | `src/main/resources/static/style.css` | Weather card, risk badge, stats grid |

## Data Model

### WeatherData POJO

```java
public class WeatherData {
    private final String city;
    private final double temperature;
    private final double precipitation;
    private final int weatherCode;
    private final List<String> riskPests;
    // constructor + getters
}
```

### FarmerForm — new field

```java
private String location;  // getter + setter
```

### ReportRecord — new field

```java
private String location;  // nullable, getter only
// add to constructor: public ReportRecord(..., String location)
```

### ReportService — new methods

```java
public long getTotalCount()          // repository.count()
public String getMostCommonPest()    // stream group-by pestName, max by count
public double getAvgDeliveryTime()   // stream mapToDouble(deliveryTime).average()
```

If `getAllReports()` returns empty list: `getMostCommonPest()` returns `"None"`, `getAvgDeliveryTime()` returns `0.0`.

## Pest Risk Rules (in WeatherService)

| Condition | High-Risk Pests |
|-----------|----------------|
| Temp ≥ 35°C AND precipitation < 2mm | Locusts, Whitefly, Aphids |
| Precipitation ≥ 5mm AND temp ≥ 20°C | Brown Planthopper, Leaf Folder, Stem Borer |
| Temp 20–34°C AND precipitation < 2mm | Aphids, Mites, Thrips |
| Temp < 20°C AND precipitation ≥ 3mm | Armyworm, Cutworm |

Rules applied in order; multiple rules can match; resulting list deduplicated.

## Open-Meteo API Calls

### Geocoding

```
GET https://geocoding-api.open-meteo.com/v1/search?name={city}&count=1&language=en&format=json
```

Response (success):
```json
{"results": [{"name": "Mumbai", "latitude": 19.07, "longitude": 72.87, "country": "India"}]}
```

Response (not found): `{}` or `{"results": []}` — return null from WeatherService.

### Weather Forecast

```
GET https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lng}&current=temperature_2m,precipitation,weathercode&timezone=auto
```

Response:
```json
{"current": {"temperature_2m": 31.5, "precipitation": 0.2, "weathercode": 2}}
```

Both parsed via `RestTemplate.getForObject(url, Map.class)` + Jackson (already in classpath).

## Result Page Additions

### Weather Card (shown only if WeatherData != null)

```
┌──────────────────────────────────────────┐
│ 🌤 Weather in Mumbai                      │
│ 31.5°C  ·  0.2mm precipitation           │
│                                           │
│ ⚠ High-Risk Pests: Locusts · Whitefly    │
└──────────────────────────────────────────┘
```

### Stats Row (always shown when ≥ 1 report exists)

```
📊 Total Reports: 5  ·  Most Common: Aphids  ·  Avg Delivery: 28.3 min
```

## Success Criteria

- User types "Mumbai" in Step 1 → result shows Mumbai weather card
- High-risk pests listed match the conditions (e.g., hot+dry → Locusts, Whitefly, Aphids)
- Blank location → weather card hidden, stats still shown
- Unknown city (geocoding returns no results) → weather card hidden gracefully
- Stats row shows correct count after each submission
- Existing 32 tests still pass
- New WeatherServiceTest: pest risk logic tests pass (no HTTP calls needed)
- `mvn spring-boot:run` starts cleanly
