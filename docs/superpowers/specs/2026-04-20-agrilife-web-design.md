# AgriLife Web App Design

**Date:** 2026-04-20

## Goal

Convert existing plain Java CLI app into Spring Boot + Thymeleaf web app with agricultural theme.

## Tech Stack

- **Backend:** Spring Boot 3.x (Maven)
- **Templating:** Thymeleaf (server-rendered HTML)
- **Frontend:** HTML + CSS (no JS framework), agricultural theme
- **Build:** Maven (`pom.xml`)

## Project Structure

```
AgriLife/
├── pom.xml
├── src/main/java/agrilife/
│   ├── AgriLifeApplication.java     (Spring Boot entry point)
│   ├── Farmer.java                  (keep existing)
│   ├── PestService.java             (keep existing, minor refactor)
│   ├── DeliveryService.java         (keep existing, minor refactor)
│   └── AgriLifeController.java      (new - MVC controller)
└── src/main/resources/
    ├── templates/
    │   ├── index.html               (form page)
    │   └── result.html              (results page)
    └── static/
        └── style.css               (agricultural theme - greens, earth tones)
```

## User Flow

1. `GET /` → renders `index.html` with input form
2. User fills: farmer name, phone, pest type (dropdown of 7), distance (km), speed (km/h)
3. `POST /recommend` → controller calls PestService + DeliveryService → renders `result.html`
4. Result page shows: farmer info, pesticide recommendation, delivery time estimate
5. "Back" button returns to form

## Components

### AgriLifeController
- `GET /` → returns `index.html`
- `POST /recommend` → accepts form params, builds Farmer, calls services, adds result to Model, returns `result.html`

### PestService (refactor)
- Extract `getRecommendation(String pestName)` returning `String` (instead of void print)
- Keep existing 7 pest mappings

### DeliveryService (refactor)
- Extract `calculateDeliveryMinutes(double distance, double speed)` returning `double`
- Keep existing formula: `(distance × 60) / speed + 5`

### index.html
- Form: name, phone, pest dropdown (7 options), distance, speed
- Agricultural theme: green header, farm imagery via CSS, earthy colors

### result.html
- Shows farmer details, pesticide recommendation, delivery estimate
- Agricultural theme consistent with index
- "New Query" button back to `/`

### style.css
- Color palette: deep green (#2d5a27), wheat/tan (#d4a853), white
- Font: system sans-serif
- Card-based layout, responsive

## What Changes vs Current Code

| File | Action |
|------|--------|
| `MainApp.java` | Remove (CLI replaced by web) |
| `Farmer.java` | Keep as-is |
| `PestService.java` | Refactor: add return-value method |
| `DeliveryService.java` | Refactor: add return-value method |
| `AgriLifeApplication.java` | New |
| `AgriLifeController.java` | New |
| `index.html` | New |
| `result.html` | New |
| `style.css` | New |
| `pom.xml` | New |

## Success Criteria

- `mvn spring-boot:run` starts app on port 8080
- Form submits and shows correct pesticide for each of 7 pests
- Delivery time calculates correctly
- Pages render with agricultural theme
