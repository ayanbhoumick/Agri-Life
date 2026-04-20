# AgriLife Phase 2: Persistence + Report History Design

**Date:** 2026-04-20

## Goal

Persist every farmer submission to an embedded H2 database and expose a read-only report history page at `/reports` with inline row expansion.

## Tech Stack

Existing: Java 17, Spring Boot 3.2.5, Thymeleaf, Maven  
New: Spring Data JPA (`spring-boot-starter-data-jpa`), H2 (`com.h2database:h2`, runtime scope)

## Architecture

Single `@Entity` class maps to one auto-created H2 table. A `@Repository` interface handles all DB access. A `@Service` wraps save and query. A new `@Controller` serves the history page. The existing `AgriLifeController` calls the service after delivery calculation.

No SQL files needed — Spring Boot generates the schema from the entity on startup (`spring.jpa.hibernate.ddl-auto=update`).

H2 console disabled in production profile. Data persists in-memory for the session (resets on restart) — appropriate for a college demo project.

## File Changes

| Action | Path | Responsibility |
|--------|------|---------------|
| Create | `src/main/java/agrilife/ReportRecord.java` | `@Entity` — one row per submission |
| Create | `src/main/java/agrilife/ReportRepository.java` | `JpaRepository` — `findAllByOrderByCreatedAtDesc()` |
| Create | `src/main/java/agrilife/ReportService.java` | `@Service` — save + getAllReports |
| Create | `src/main/java/agrilife/ReportsController.java` | `@Controller` — GET /reports |
| Create | `src/main/resources/templates/reports.html` | History table with inline accordion expansion |
| Create | `src/test/java/agrilife/ReportServiceTest.java` | Save + retrieve tests |
| Modify | `src/main/java/agrilife/AgriLifeController.java` | Inject ReportService, call save() in POST /recommend |
| Modify | `src/main/resources/templates/result.html` | Add "View All Reports →" link |
| Modify | `src/main/resources/static/style.css` | Report table, accordion row, expand panel styles |
| Modify | `pom.xml` | Add spring-boot-starter-data-jpa + h2 dependencies |

## Data Model

### ReportRecord Entity

| Field | Type | Notes |
|-------|------|-------|
| id | Long | `@Id @GeneratedValue` auto-increment |
| farmerName | String | from FarmerForm.name |
| phone | String | from FarmerForm.phone |
| cropName | String | from FarmerForm.cropName |
| pestName | String | from FarmerForm.pestName |
| pesticide | String | result of PestService.recommendPesticide() |
| deliveryTime | double | calculated delivery time in minutes |
| distance | double | from FarmerForm.distance |
| speed | double | effectiveSpeed used in calculation |
| createdAt | LocalDateTime | `@Column` set to `LocalDateTime.now()` on save |

### ReportRepository

```java
@Repository
public interface ReportRepository extends JpaRepository<ReportRecord, Long> {
    List<ReportRecord> findAllByOrderByCreatedAtDesc();
}
```

### application.properties additions

```properties
spring.datasource.url=jdbc:h2:mem:agrilife
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

## Page Design: /reports

```
┌─────────────────────────────────────────────────┐
│ 📋 Report History              [3 reports]       │
│─────────────────────────────────────────────────│
│ FARMER    CROP    PEST    PESTICIDE    TIME  DATE │
│─────────────────────────────────────────────────│
│ Rajesh    Rice    Brown   Buprofezin  35.0m  Apr │
│  ▼ [expanded inline detail panel]               │
│─────────────────────────────────────────────────│
│ Priya     Cotton  Bollworm Emamectin  22.5m  Apr │
│─────────────────────────────────────────────────│
│                          ← Back to New Report   │
└─────────────────────────────────────────────────┘
```

- Click any row → toggles `.expanded` class → detail panel slides in below
- Detail panel shows: all fields including phone, distance, speed
- Read-only — no delete, no edit
- Newest first (sorted by createdAt DESC)
- Empty state: "No reports yet. Submit your first report →" link to /

## JS Logic (client-side, reports.html)

```js
function toggleRow(id) {
    const detail = document.getElementById('detail-' + id);
    detail.classList.toggle('expanded');
}
```

No fetch, no AJAX — all data pre-loaded in Thymeleaf template.

## Navigation

- Header on `result.html` gets a "View All Reports →" link after the result card
- All pages keep existing header (no nav bar needed — college project scope)

## Success Criteria

- Every POST /recommend saves one row to H2
- GET /reports shows all saved reports, newest first
- Click row expands inline detail, click again collapses
- Empty state renders cleanly when no reports exist
- Existing 28 tests still pass
- `mvn spring-boot:run` starts cleanly with JPA
- New ReportServiceTest: at least save + findAll + ordering tests pass
