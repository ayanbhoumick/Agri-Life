# AgriLife Phase 1: Crop Lookup + Wizard UI Design

**Date:** 2026-04-20

## Goal

Upgrade AgriLife from a single-form page to a polished 4-step wizard with Indian crop-based pest filtering, real pest photos, and 12 pests (7 existing + 5 India-specific).

## Tech Stack

Existing: Java 17, Spring Boot 3.2.5, Thymeleaf, Maven
New: Vanilla JS (wizard logic, crop→pest filter), Wikipedia Commons public-domain image URLs

## Architecture

Single Thymeleaf page (`index.html`) rewired as a 4-panel wizard. JS shows/hides panels and tracks current step. One `POST /recommend` fires at end of step 4 — no session state needed. Backend passes crop list + pest metadata to the template via Model.

## File Changes

| File | Action | Responsibility |
|------|--------|---------------|
| `CropService.java` | Create | `@Service` — 8 Indian crops, each mapped to list of pest names |
| `Crop.java` | Create | POJO: cropName, emoji icon, list of pest names |
| `PestService.java` | Modify | Add 5 new pests + photo URL map for all 12 pests |
| `AgriLifeController.java` | Modify | Pass crops list + pest-photo map to `index` model |
| `FarmerForm.java` | Modify | Add `cropName` field |
| `index.html` | Rewrite | 4-step wizard with JS step logic, crop cards, pest photo cards |
| `result.html` | Modify | Show pest photo + crop name in result |
| `style.css` | Modify | Progress bar, crop card grid, pest photo card, step transitions |

## Data Model

### 8 Indian Crops → Pest Mapping

| Crop | Emoji | Relevant Pests |
|------|-------|---------------|
| Rice | 🌾 | Brown Planthopper, Stem Borer, Leaf Folder, Armyworm |
| Wheat | 🌿 | Aphids, Armyworm, Locusts |
| Maize | 🌽 | Armyworm, Cutworm, Stem Borer, Locusts |
| Cotton | 🌸 | Bollworm, Whitefly, Thrips, Mites |
| Tomato | 🍅 | Whitefly, Aphids, Mites, Cutworm |
| Potato | 🥔 | Aphids, Cutworm, Thrips |
| Chili | 🌶️ | Thrips, Whitefly, Mites, Jassid |
| Onion | 🧅 | Thrips, Armyworm |

### 12 Pests → Pesticide + Photo URL

| Pest | Pesticide | Photo source |
|------|-----------|-------------|
| Aphids | Imidacloprid | Wikipedia Commons |
| Armyworm | Spinosad | Wikipedia Commons |
| Whitefly | Acetamiprid | Wikipedia Commons |
| Thrips | Abamectin | Wikipedia Commons |
| Mites / Spider Mites | Bifenazate | Wikipedia Commons |
| Locusts | Chlorpyrifos | Wikipedia Commons |
| Cutworm | Lambda-cyhalothrin | Wikipedia Commons |
| Brown Planthopper | Buprofezin | Wikipedia Commons |
| Stem Borer | Chlorantraniliprole | Wikipedia Commons |
| Bollworm | Emamectin Benzoate | Wikipedia Commons |
| Leaf Folder | Cartap Hydrochloride | Wikipedia Commons |
| Jassid | Dimethoate | Wikipedia Commons |

Photo URLs resolved at implementation time from Wikipedia Commons — no download, inline `<img src="...">`.

## Wizard Flow

```
Progress bar: ● ● ○ ○  (fills per step)

Step 1 — Farmer Info
  Fields: Name (text), Phone (tel)
  Next → enabled always

Step 2 — Select Crop
  8 crop cards in 2×4 grid: emoji + name
  Click = select (green highlight)
  Next → enabled only after selection
  ← Back to Step 1

Step 3 — Select Pest
  Shows only pests matching selected crop
  Each pest: photo (80×80px) + name card
  Click = select
  Next → enabled only after selection
  ← Back to Step 2

Step 4 — Delivery Details
  Fields: Distance (km, number), Speed (km/h, number)
  Submit → POST /recommend
  ← Back to Step 3

Result Page
  Pest photo (larger)
  Crop name displayed
  Recommended pesticide (highlight box)
  Estimated delivery time (highlight box)
  Farmer details table
  ← New Report
```

## JS Logic (client-side only)

- `cropPestMap` — JS object mapping cropName → array of pestNames (embedded in template by Thymeleaf)
- `pestPhotoMap` — JS object mapping pestName → photo URL
- `showStep(n)` — hides all panels, shows panel n, updates progress bar dots
- Step 2 selection updates hidden `cropName` input, filters step 3 pest cards
- Step 3 selection updates hidden `pestName` input
- Back buttons call `showStep(n-1)`
- No AJAX, no fetch — all data pre-loaded in page

## Success Criteria

- All 8 crops selectable with correct pest subset shown
- All 12 pests have a photo displayed
- Progress bar advances correctly across 4 steps
- Back navigation works at every step
- Form submits correctly with name, phone, cropName, pestName, distance, speed
- Result page shows pest photo + crop name
- Existing 14 unit tests still pass
- `mvn spring-boot:run` starts cleanly
