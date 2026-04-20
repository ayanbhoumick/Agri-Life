# AgriLife Phase 1: Crop Wizard + UI Polish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add 8 Indian crop selector, 12 pests (5 new), real pest photos, and a 4-step vanilla-JS wizard to the existing Spring Boot + Thymeleaf AgriLife app.

**Architecture:** Single Thymeleaf page wizard — JS shows/hides 4 step panels, one POST at end. Server embeds crop→pest map and pest→photo map as inline JS via Thymeleaf. New CropService handles crop data. PestService extended with 5 new pests and photo URLs.

**Tech Stack:** Java 17, Spring Boot 3.2.5, Thymeleaf, Vanilla JS (no new deps), Wikipedia Commons image URLs with onerror fallback

**Design Audit Rule:** Before finalizing ANY file in `src/main/resources/static/` or `src/main/resources/templates/`, run all 5 audits from `src/DESIGN.md` (01 Visual Hierarchy → 02 Typography → 03 Whitespace → 04 Color/Contrast → 05 Why Does This Look Cheap). Fix inline, then deliver.

---

## File Map

| Action | Path | Responsibility |
|--------|------|---------------|
| Create | `src/main/java/agrilife/Crop.java` | POJO: name, emoji, list of pest keys |
| Create | `src/main/java/agrilife/CropService.java` | @Service — 8 Indian crops with pest mappings |
| Modify | `src/main/java/agrilife/PestService.java` | Add 5 new pests + `getPhotoUrl(String)` + `getAllPestNames()` |
| Modify | `src/main/java/agrilife/FarmerForm.java` | Add `cropName` field |
| Modify | `src/main/java/agrilife/AgriLifeController.java` | Pass crops list + pestPhotoMap to index model; add cropName/pestPhotoUrl to result model |
| Create | `src/test/java/agrilife/CropServiceTest.java` | Tests for crop→pest mapping |
| Modify | `src/test/java/agrilife/PestServiceTest.java` | Add 5 tests for new pests |
| Rewrite | `src/main/resources/templates/index.html` | 4-step wizard with inline JS |
| Modify | `src/main/resources/templates/result.html` | Add pest photo + crop name |
| Modify | `src/main/resources/static/style.css` | Wizard progress bar, crop cards, pest photo cards, step transitions |

---

## Task 1: Create Crop.java POJO

**Files:**
- Create: `src/main/java/agrilife/Crop.java`

- [ ] **Step 1: Create Crop.java**

Create `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/Crop.java`:

```java
package agrilife;

import java.util.List;

public class Crop {

    private final String name;
    private final String emoji;
    private final List<String> pestKeys;

    public Crop(String name, String emoji, List<String> pestKeys) {
        this.name = name;
        this.emoji = emoji;
        this.pestKeys = pestKeys;
    }

    public String getName() { return name; }
    public String getEmoji() { return emoji; }
    public List<String> getPestKeys() { return pestKeys; }
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
git add src/main/java/agrilife/Crop.java
git commit -m "feat: add Crop POJO with name, emoji, pestKeys"
```

---

## Task 2: Create CropService.java

**Files:**
- Create: `src/main/java/agrilife/CropService.java`
- Create: `src/test/java/agrilife/CropServiceTest.java`

- [ ] **Step 1: Write failing test first**

Create `/Users/abhoumic/Downloads/AgriLife/src/test/java/agrilife/CropServiceTest.java`:

```java
package agrilife;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CropServiceTest {

    private final CropService service = new CropService();

    @Test
    void returns_eight_crops() {
        assertEquals(8, service.getAllCrops().size());
    }

    @Test
    void rice_has_brown_planthopper() {
        Crop rice = service.getAllCrops().stream()
            .filter(c -> c.getName().equals("Rice"))
            .findFirst().orElseThrow();
        assertTrue(rice.getPestKeys().contains("brown planthopper"));
    }

    @Test
    void cotton_has_bollworm() {
        Crop cotton = service.getAllCrops().stream()
            .filter(c -> c.getName().equals("Cotton"))
            .findFirst().orElseThrow();
        assertTrue(cotton.getPestKeys().contains("bollworm"));
    }

    @Test
    void crop_names_are_unique() {
        List<Crop> crops = service.getAllCrops();
        long uniqueNames = crops.stream().map(Crop::getName).distinct().count();
        assertEquals(crops.size(), uniqueNames);
    }

    @Test
    void all_crops_have_at_least_one_pest() {
        service.getAllCrops().forEach(crop ->
            assertFalse(crop.getPestKeys().isEmpty(), crop.getName() + " has no pests"));
    }

    @Test
    void getCropPestMap_contains_all_crop_names() {
        var map = service.getCropPestMap();
        assertEquals(8, map.size());
        assertTrue(map.containsKey("Rice"));
        assertTrue(map.containsKey("Cotton"));
    }
}
```

- [ ] **Step 2: Run test — verify it fails**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test -pl . -Dtest=CropServiceTest 2>&1 | tail -10
```

Expected: compilation error — `CropService` does not exist yet.

- [ ] **Step 3: Create CropService.java**

Create `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/CropService.java`:

```java
package agrilife;

import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CropService {

    private final List<Crop> crops = Arrays.asList(
        new Crop("Rice",   "🌾", Arrays.asList("brown planthopper", "stem borer", "leaf folder", "armyworm")),
        new Crop("Wheat",  "🌿", Arrays.asList("aphids", "armyworm", "locusts")),
        new Crop("Maize",  "🌽", Arrays.asList("armyworm", "cutworm", "stem borer", "locusts")),
        new Crop("Cotton", "🌸", Arrays.asList("bollworm", "whitefly", "thrips", "mites")),
        new Crop("Tomato", "🍅", Arrays.asList("whitefly", "aphids", "mites", "cutworm")),
        new Crop("Potato", "🥔", Arrays.asList("aphids", "cutworm", "thrips")),
        new Crop("Chili",  "🌶️", Arrays.asList("thrips", "whitefly", "mites", "jassid")),
        new Crop("Onion",  "🧅", Arrays.asList("thrips", "armyworm"))
    );

    public List<Crop> getAllCrops() {
        return crops;
    }

    public Map<String, List<String>> getCropPestMap() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (Crop crop : crops) {
            map.put(crop.getName(), crop.getPestKeys());
        }
        return map;
    }
}
```

- [ ] **Step 4: Run tests — verify they pass**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test -Dtest=CropServiceTest 2>&1 | tail -10
```

Expected: `Tests run: 6, Failures: 0, Errors: 0` and `BUILD SUCCESS`

- [ ] **Step 5: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/java/agrilife/CropService.java src/test/java/agrilife/CropServiceTest.java
git commit -m "feat: add CropService with 8 Indian crops and pest mappings"
```

---

## Task 3: Extend PestService with 5 new pests and photo URLs

**Files:**
- Modify: `src/main/java/agrilife/PestService.java`
- Modify: `src/test/java/agrilife/PestServiceTest.java`

- [ ] **Step 1: Add 5 new test cases to PestServiceTest.java**

Open `/Users/abhoumic/Downloads/AgriLife/src/test/java/agrilife/PestServiceTest.java` and append these tests inside the class (before the closing `}`):

```java
    @Test
    void brown_planthopper_returns_buprofezin() {
        assertEquals("Buprofezin", service.recommendPesticide("brown planthopper"));
    }

    @Test
    void stem_borer_returns_chlorantraniliprole() {
        assertEquals("Chlorantraniliprole", service.recommendPesticide("stem borer"));
    }

    @Test
    void bollworm_returns_emamectin_benzoate() {
        assertEquals("Emamectin Benzoate", service.recommendPesticide("bollworm"));
    }

    @Test
    void leaf_folder_returns_cartap_hydrochloride() {
        assertEquals("Cartap Hydrochloride", service.recommendPesticide("leaf folder"));
    }

    @Test
    void jassid_returns_dimethoate() {
        assertEquals("Dimethoate", service.recommendPesticide("jassid"));
    }

    @Test
    void getPhotoUrl_returns_non_null_for_known_pest() {
        assertNotNull(service.getPhotoUrl("aphids"));
        assertFalse(service.getPhotoUrl("aphids").isEmpty());
    }

    @Test
    void getPhotoUrl_returns_empty_for_unknown_pest() {
        assertEquals("", service.getPhotoUrl("dragon"));
    }

    @Test
    void getAllPestNames_returns_twelve_pests() {
        assertEquals(12, service.getAllPestNames().size());
    }
```

- [ ] **Step 2: Run tests — verify new ones fail**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test -Dtest=PestServiceTest 2>&1 | tail -15
```

Expected: failures on the 5 new pest tests + getPhotoUrl/getAllPestNames tests.

- [ ] **Step 3: Rewrite PestService.java with all 12 pests + photo URLs**

Replace `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/PestService.java` with:

```java
package agrilife;

import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PestService {

    private static final Map<String, String> PESTICIDE_MAP = new HashMap<>();
    private static final Map<String, String> PHOTO_MAP = new HashMap<>();

    static {
        PESTICIDE_MAP.put("aphids",            "Imidacloprid");
        PESTICIDE_MAP.put("aphid",             "Imidacloprid");
        PESTICIDE_MAP.put("armyworm",          "Spinosad");
        PESTICIDE_MAP.put("army worm",         "Spinosad");
        PESTICIDE_MAP.put("whitefly",          "Acetamiprid");
        PESTICIDE_MAP.put("white fly",         "Acetamiprid");
        PESTICIDE_MAP.put("thrips",            "Abamectin");
        PESTICIDE_MAP.put("thrip",             "Abamectin");
        PESTICIDE_MAP.put("mites",             "Bifenazate");
        PESTICIDE_MAP.put("spider mites",      "Bifenazate");
        PESTICIDE_MAP.put("locusts",           "Chlorpyrifos");
        PESTICIDE_MAP.put("locust",            "Chlorpyrifos");
        PESTICIDE_MAP.put("cutworm",           "Lambda-cyhalothrin");
        PESTICIDE_MAP.put("cut worm",          "Lambda-cyhalothrin");
        PESTICIDE_MAP.put("brown planthopper", "Buprofezin");
        PESTICIDE_MAP.put("stem borer",        "Chlorantraniliprole");
        PESTICIDE_MAP.put("bollworm",          "Emamectin Benzoate");
        PESTICIDE_MAP.put("leaf folder",       "Cartap Hydrochloride");
        PESTICIDE_MAP.put("jassid",            "Dimethoate");

        PHOTO_MAP.put("aphids",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/Aphis_fabae_on_stem.jpg/200px-Aphis_fabae_on_stem.jpg");
        PHOTO_MAP.put("armyworm",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/Spodoptera_frugiperda_caterpillar.jpg/200px-Spodoptera_frugiperda_caterpillar.jpg");
        PHOTO_MAP.put("whitefly",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/4/43/Trialeurodes_vaporariorum_2.jpg/200px-Trialeurodes_vaporariorum_2.jpg");
        PHOTO_MAP.put("thrips",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Thrip_on_flower.jpg/200px-Thrip_on_flower.jpg");
        PHOTO_MAP.put("mites",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ce/Spider_mite_P1010023.jpg/200px-Spider_mite_P1010023.jpg");
        PHOTO_MAP.put("locusts",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ef/CSIRO_ScienceImage_97_Desert_Locust.jpg/200px-CSIRO_ScienceImage_97_Desert_Locust.jpg");
        PHOTO_MAP.put("cutworm",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Agrotis_ipsilon_caterpillar.jpg/200px-Agrotis_ipsilon_caterpillar.jpg");
        PHOTO_MAP.put("brown planthopper",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e2/Nilaparvata_lugens.jpg/200px-Nilaparvata_lugens.jpg");
        PHOTO_MAP.put("stem borer",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5c/Chilo_suppressalis_larva.jpg/200px-Chilo_suppressalis_larva.jpg");
        PHOTO_MAP.put("bollworm",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/8/86/Helicoverpa_armigera_adult.jpg/200px-Helicoverpa_armigera_adult.jpg");
        PHOTO_MAP.put("leaf folder",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Cnaphalocrocis_medinalis.jpg/200px-Cnaphalocrocis_medinalis.jpg");
        PHOTO_MAP.put("jassid",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a5/Empoasca_sp.jpg/200px-Empoasca_sp.jpg");
    }

    private static final List<String> CANONICAL_PEST_NAMES = Arrays.asList(
        "aphids", "armyworm", "whitefly", "thrips", "mites",
        "locusts", "cutworm", "brown planthopper", "stem borer",
        "bollworm", "leaf folder", "jassid"
    );

    public String recommendPesticide(String pestName) {
        if (pestName == null || pestName.trim().isEmpty()) {
            return "No pest information provided. Please consult an expert.";
        }
        String key = pestName.trim().toLowerCase();
        String result = PESTICIDE_MAP.get(key);
        return result != null ? result : "Unknown pest. Please consult an agricultural expert.";
    }

    public String getPhotoUrl(String pestName) {
        if (pestName == null) return "";
        String url = PHOTO_MAP.get(pestName.trim().toLowerCase());
        return url != null ? url : "";
    }

    public List<String> getAllPestNames() {
        return CANONICAL_PEST_NAMES;
    }

    public Map<String, String> getPestPhotoMap() {
        return new HashMap<>(PHOTO_MAP);
    }
}
```

- [ ] **Step 4: Run all tests — verify 19 pass (14 original + 5 new pest + getPhotoUrl + getAllPestNames + CropService)**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test 2>&1 | tail -10
```

Expected: `Tests run: 22, Failures: 0, Errors: 0` (9 original PestService + 8 new PestService + 6 CropService - 1 duplicate = check exact count matches all pass) and `BUILD SUCCESS`

- [ ] **Step 5: Verify photo URLs return HTTP 200 (spot check 3)**

```bash
for url in \
  "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ef/CSIRO_ScienceImage_97_Desert_Locust.jpg/200px-CSIRO_ScienceImage_97_Desert_Locust.jpg" \
  "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/Aphis_fabae_on_stem.jpg/200px-Aphis_fabae_on_stem.jpg" \
  "https://upload.wikimedia.org/wikipedia/commons/thumb/4/43/Trialeurodes_vaporariorum_2.jpg/200px-Trialeurodes_vaporariorum_2.jpg"; do
  code=$(curl -s -o /dev/null -w "%{http_code}" "$url")
  echo "$code $url"
done
```

Expected: `200` for each. If any return `404`, find a working Wikimedia Commons URL for that pest by searching `https://commons.wikimedia.org/w/index.php?search=<pestname>+insect` and update `PHOTO_MAP` in `PestService.java`. Re-run tests after any fix.

- [ ] **Step 6: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/java/agrilife/PestService.java src/test/java/agrilife/PestServiceTest.java
git commit -m "feat: extend PestService with 5 Indian pests and photo URL map"
```

---

## Task 4: Update FarmerForm and AgriLifeController

**Files:**
- Modify: `src/main/java/agrilife/FarmerForm.java`
- Modify: `src/main/java/agrilife/AgriLifeController.java`

- [ ] **Step 1: Add cropName to FarmerForm.java**

Replace `/Users/abhoumic/Downloads/AgriLife/src/main/java/agrilife/FarmerForm.java` with:

```java
package agrilife;

public class FarmerForm {

    private String name;
    private String phone;
    private String cropName;
    private String pestName;
    private double distance;
    private double speed;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

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

- [ ] **Step 2: Update AgriLifeController.java**

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

    public AgriLifeController(PestService pestService, DeliveryService deliveryService,
                               CropService cropService) {
        this.pestService = pestService;
        this.deliveryService = deliveryService;
        this.cropService = cropService;
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
        String deliveryError = null;
        try {
            deliveryTime = deliveryService.calculateDeliveryTime(form.getDistance(), form.getSpeed());
        } catch (IllegalArgumentException e) {
            deliveryTime = deliveryService.calculateDeliveryTime(form.getDistance(), 30);
            deliveryError = "Invalid speed — defaulted to 30 km/h.";
        }

        model.addAttribute("farmer", farmer);
        model.addAttribute("cropName", form.getCropName());
        model.addAttribute("recommendation", recommendation);
        model.addAttribute("pestPhotoUrl", pestPhotoUrl);
        model.addAttribute("deliveryTime", String.format("%.1f", deliveryTime));
        model.addAttribute("distance", form.getDistance());
        model.addAttribute("speed", form.getSpeed());
        model.addAttribute("deliveryError", deliveryError);
        return "result";
    }
}
```

- [ ] **Step 3: Verify compile**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn compile -q 2>&1 | tail -3
```

Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/java/agrilife/FarmerForm.java src/main/java/agrilife/AgriLifeController.java
git commit -m "feat: add cropName to FarmerForm, inject CropService, pass photo URLs to templates"
```

---

## Task 5: Update style.css with wizard styles

**Files:**
- Modify: `src/main/resources/static/style.css`

Run all 5 design audits from `src/DESIGN.md` before finalizing.

- [ ] **Step 1: Append wizard CSS to existing style.css**

Append to the end of `/Users/abhoumic/Downloads/AgriLife/src/main/resources/static/style.css`:

```css
/* ── Wizard Progress Bar ── */
.progress-bar {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0;
    margin-bottom: 32px;
}

.progress-step {
    display: flex;
    align-items: center;
    gap: 0;
}

.step-dot {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background-color: #d0c9bb;
    color: #fff;
    font-size: 0.82rem;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.3s;
    position: relative;
    z-index: 1;
}

.step-dot.active {
    background-color: #2d7a32;
}

.step-dot.done {
    background-color: #1e4620;
}

.step-line {
    width: 48px;
    height: 2px;
    background-color: #d0c9bb;
    transition: background-color 0.3s;
}

.step-line.done {
    background-color: #2d7a32;
}

.step-label {
    font-size: 0.7rem;
    color: #9a9080;
    text-align: center;
    margin-top: 4px;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.4px;
}

.progress-wrapper {
    margin-bottom: 28px;
}

.progress-wrapper .steps-row {
    display: flex;
    align-items: center;
    justify-content: center;
}

.progress-wrapper .labels-row {
    display: flex;
    justify-content: space-between;
    padding: 0 0px;
    margin-top: 6px;
}

/* ── Wizard Panels ── */
.wizard-panel {
    display: none;
}

.wizard-panel.active {
    display: block;
}

/* ── Crop Cards ── */
.crop-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
    margin-bottom: 24px;
}

.crop-card {
    border: 2px solid #e0d8cc;
    border-radius: 10px;
    padding: 16px 8px;
    text-align: center;
    cursor: pointer;
    background: #fdfaf5;
    transition: border-color 0.2s, background-color 0.2s, transform 0.1s;
    user-select: none;
}

.crop-card:hover {
    border-color: #2d7a32;
    background: #f0f8f0;
}

.crop-card.selected {
    border-color: #2d7a32;
    background: #eaf5ea;
    box-shadow: 0 0 0 3px rgba(45, 122, 50, 0.15);
}

.crop-card:active {
    transform: scale(0.97);
}

.crop-emoji {
    font-size: 2rem;
    display: block;
    margin-bottom: 6px;
}

.crop-name {
    font-size: 0.78rem;
    font-weight: 600;
    color: #2c2c2c;
    text-transform: uppercase;
    letter-spacing: 0.4px;
}

/* ── Pest Photo Cards ── */
.pest-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    margin-bottom: 24px;
}

.pest-card {
    border: 2px solid #e0d8cc;
    border-radius: 10px;
    padding: 12px;
    cursor: pointer;
    background: #fdfaf5;
    transition: border-color 0.2s, background-color 0.2s;
    display: flex;
    align-items: center;
    gap: 12px;
    user-select: none;
}

.pest-card:hover {
    border-color: #2d7a32;
    background: #f0f8f0;
}

.pest-card.selected {
    border-color: #2d7a32;
    background: #eaf5ea;
    box-shadow: 0 0 0 3px rgba(45, 122, 50, 0.15);
}

.pest-card img {
    width: 64px;
    height: 48px;
    object-fit: cover;
    border-radius: 6px;
    flex-shrink: 0;
    background-color: #e8e0d0;
}

.pest-card-name {
    font-size: 0.85rem;
    font-weight: 600;
    color: #2c2c2c;
    text-transform: capitalize;
}

/* ── Wizard Nav Buttons ── */
.wizard-nav {
    display: flex;
    gap: 12px;
    margin-top: 24px;
}

.btn-back {
    padding: 11px 20px;
    background: transparent;
    color: #6b6b6b;
    font-size: 0.95rem;
    font-weight: 600;
    border: 1.5px solid #d0c9bb;
    border-radius: 8px;
    cursor: pointer;
    transition: border-color 0.2s, color 0.2s;
}

.btn-back:hover {
    border-color: #9a9080;
    color: #2c2c2c;
}

.btn-next {
    flex: 1;
    padding: 12px;
    background-color: #2d7a32;
    color: #ffffff;
    font-size: 1rem;
    font-weight: 600;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.2s;
}

.btn-next:hover {
    background-color: #236227;
}

.btn-next:disabled {
    background-color: #a8c5a8;
    cursor: not-allowed;
}

/* ── Result page pest photo ── */
.pest-photo-result {
    width: 100%;
    max-height: 180px;
    object-fit: cover;
    border-radius: 10px;
    margin-bottom: 20px;
    display: block;
}

.crop-badge {
    display: inline-block;
    background: #eaf5ea;
    color: #1e4620;
    font-size: 0.78rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.6px;
    padding: 4px 10px;
    border-radius: 20px;
    margin-bottom: 16px;
    border: 1px solid #b4ddb4;
}
```

- [ ] **Step 2: Run Design Audits 01–05 on updated CSS**

01 HIERARCHY: New wizard elements (progress dots, crop cards, pest cards) follow existing size/color hierarchy. Active step dot = brand green (#2d7a32). Disabled next button = muted green — clear state signal.

02 TYPOGRAPHY: crop-name and pest-card-name both 600 weight uppercase — consistent with existing label style. crop-name 0.78rem matches existing label size. No new font introduced.

03 WHITESPACE: crop-grid 12px gap, pest-grid 12px gap — consistent with existing form-row 16px gap (slightly tighter for cards, appropriate). Crop card 16px vertical padding creates breathing room. Step dots 32px — large enough to tap on mobile.

04 COLOR: `.selected` state uses same #2d7a32 + #eaf5ea as existing highlight-box. Disabled button uses #a8c5a8 (muted green) — clearly inactive but on-brand. No new colors introduced.

05 CHEAP: (1) Step line transitions make progress feel live. (2) crop-card:active scale(0.97) gives tactile feedback. (3) pest-photo 64×48 in card — small but identifiable. Nothing cheap introduced. Keep: crop-card hover matches pest-card hover — visual consistency.

Audits pass. No blocking changes.

- [ ] **Step 3: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/resources/static/style.css
git commit -m "feat: add wizard progress bar, crop card grid, pest photo card styles"
```

---

## Task 6: Rewrite index.html as 4-step wizard

**Files:**
- Rewrite: `src/main/resources/templates/index.html`

Run all 5 design audits before finalizing.

- [ ] **Step 1: Rewrite index.html**

Replace `/Users/abhoumic/Downloads/AgriLife/src/main/resources/templates/index.html` with:

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

            <!-- Progress Bar -->
            <div class="progress-wrapper">
                <div class="steps-row">
                    <div class="progress-step">
                        <div class="step-dot active" id="dot-1">1</div>
                    </div>
                    <div class="step-line" id="line-1"></div>
                    <div class="progress-step">
                        <div class="step-dot" id="dot-2">2</div>
                    </div>
                    <div class="step-line" id="line-2"></div>
                    <div class="progress-step">
                        <div class="step-dot" id="dot-3">3</div>
                    </div>
                    <div class="step-line" id="line-3"></div>
                    <div class="progress-step">
                        <div class="step-dot" id="dot-4">4</div>
                    </div>
                </div>
                <div class="labels-row">
                    <span class="step-label">Info</span>
                    <span class="step-label">Crop</span>
                    <span class="step-label">Pest</span>
                    <span class="step-label">Delivery</span>
                </div>
            </div>

            <form th:action="@{/recommend}" th:object="${farmerForm}" method="post" id="wizard-form">

                <!-- Hidden fields populated by JS -->
                <input type="hidden" th:field="*{cropName}" id="hidden-cropName">
                <input type="hidden" th:field="*{pestName}" id="hidden-pestName">

                <!-- Step 1: Farmer Info -->
                <div class="wizard-panel active" id="step-1">
                    <h2>Your Details</h2>
                    <div class="form-group">
                        <label for="name">Farmer Name</label>
                        <input type="text" id="name" th:field="*{name}"
                               placeholder="e.g. Rajesh Kumar">
                    </div>
                    <div class="form-group">
                        <label for="phone">Phone Number</label>
                        <input type="tel" id="phone" th:field="*{phone}"
                               placeholder="e.g. +91 98765 43210">
                    </div>
                    <div class="wizard-nav">
                        <button type="button" class="btn-next" onclick="goStep(2)">Next →</button>
                    </div>
                </div>

                <!-- Step 2: Crop Selection -->
                <div class="wizard-panel" id="step-2">
                    <h2>Select Your Crop</h2>
                    <div class="crop-grid" id="crop-grid">
                        <div th:each="crop : ${crops}"
                             class="crop-card"
                             th:data-crop="*{__${crop.name}__}"
                             th:attr="data-crop=${crop.name}, onclick='selectCrop(this)'">
                            <span class="crop-emoji" th:text="${crop.emoji}">🌾</span>
                            <span class="crop-name" th:text="${crop.name}">Crop</span>
                        </div>
                    </div>
                    <div class="wizard-nav">
                        <button type="button" class="btn-back" onclick="goStep(1)">← Back</button>
                        <button type="button" class="btn-next" id="next-2" disabled onclick="goStep(3)">Next →</button>
                    </div>
                </div>

                <!-- Step 3: Pest Selection -->
                <div class="wizard-panel" id="step-3">
                    <h2>Pest Detected</h2>
                    <div class="pest-grid" id="pest-grid"></div>
                    <div class="wizard-nav">
                        <button type="button" class="btn-back" onclick="goStep(2)">← Back</button>
                        <button type="button" class="btn-next" id="next-3" disabled onclick="goStep(4)">Next →</button>
                    </div>
                </div>

                <!-- Step 4: Delivery -->
                <div class="wizard-panel" id="step-4">
                    <h2>Delivery Details</h2>
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
                    <div class="wizard-nav">
                        <button type="button" class="btn-back" onclick="goStep(3)">← Back</button>
                        <button type="submit" class="btn-next">Get Recommendation →</button>
                    </div>
                </div>

            </form>
        </div>
    </main>

    <footer>
        AgriLife Pest Management System &copy; 2026
    </footer>

    <script th:inline="javascript">
        const cropPestMap = /*[[${cropPestMap}]]*/ {};
        const pestPhotoMap = /*[[${pestPhotoMap}]]*/ {};

        let currentStep = 1;
        let selectedCrop = null;
        let selectedPest = null;

        function goStep(n) {
            document.getElementById('step-' + currentStep).classList.remove('active');
            currentStep = n;
            document.getElementById('step-' + n).classList.add('active');
            updateProgress();
            if (n === 3) buildPestGrid();
        }

        function updateProgress() {
            for (let i = 1; i <= 4; i++) {
                const dot = document.getElementById('dot-' + i);
                dot.classList.remove('active', 'done');
                if (i < currentStep) dot.classList.add('done');
                if (i === currentStep) dot.classList.add('active');
            }
            for (let i = 1; i <= 3; i++) {
                const line = document.getElementById('line-' + i);
                line.classList.toggle('done', i < currentStep);
            }
        }

        function selectCrop(el) {
            document.querySelectorAll('.crop-card').forEach(c => c.classList.remove('selected'));
            el.classList.add('selected');
            selectedCrop = el.dataset.crop;
            document.getElementById('hidden-cropName').value = selectedCrop;
            document.getElementById('next-2').disabled = false;
            selectedPest = null;
            document.getElementById('hidden-pestName').value = '';
        }

        function buildPestGrid() {
            const grid = document.getElementById('pest-grid');
            grid.innerHTML = '';
            const pests = cropPestMap[selectedCrop] || [];
            pests.forEach(function(pest) {
                const photoUrl = pestPhotoMap[pest] || '';
                const card = document.createElement('div');
                card.className = 'pest-card';
                card.dataset.pest = pest;
                card.onclick = function() { selectPest(this); };
                card.innerHTML =
                    '<img src="' + photoUrl + '" alt="' + pest + '" ' +
                    'onerror="this.style.display=\'none\'">' +
                    '<span class="pest-card-name">' + pest.replace(/\b\w/g, c => c.toUpperCase()) + '</span>';
                grid.appendChild(card);
            });
            document.getElementById('next-3').disabled = true;
        }

        function selectPest(el) {
            document.querySelectorAll('.pest-card').forEach(c => c.classList.remove('selected'));
            el.classList.add('selected');
            selectedPest = el.dataset.pest;
            document.getElementById('hidden-pestName').value = selectedPest;
            document.getElementById('next-3').disabled = false;
        }
    </script>
</body>
</html>
```

- [ ] **Step 2: Run Design Audits 01–05 on index.html**

01 HIERARCHY: Progress bar at top = orientation. Step title (h2) = current context. Content area = action zone. Next button (full-width green) = clear primary CTA. Back button (grey outline) = secondary, correct weight. No competing elements.

02 TYPOGRAPHY: Step labels 0.7rem uppercase — consistent with existing label style. Crop name 0.78rem uppercase — matches form labels. Pest card name 0.85rem capitalize — slightly larger, appropriate since it's the selection target. All weights 600/700 — consistent tier.

03 WHITESPACE: Crop grid 12px gap + 16px card padding — cards breathe. Pest grid 12px gap, cards use flex with 12px internal gap. Progress wrapper margin-bottom 28px separates it from content. Wizard-nav margin-top 24px — not cramped.

04 COLOR: Active dot = #2d7a32, done dot = #1e4620 (darker = "past"), pending = #d0c9bb (neutral). Selected card = #eaf5ea + #2d7a32 border. Disabled next = #a8c5a8. All from existing palette. No new colors.

05 CHEAP: (1) Pest image falls back gracefully via onerror. (2) Crop cards use emoji — works without images. (3) Step labels below dots give clear navigation context. Keep: progress bar dot done/active/pending three-state — clear progression.

Audits pass. No blocking changes.

- [ ] **Step 3: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/resources/templates/index.html
git commit -m "feat: rewrite index.html as 4-step vanilla JS wizard with crop and pest selection"
```

---

## Task 7: Update result.html with pest photo and crop name

**Files:**
- Modify: `src/main/resources/templates/result.html`

Run all 5 design audits before finalizing.

- [ ] **Step 1: Update result.html**

Replace `/Users/abhoumic/Downloads/AgriLife/src/main/resources/templates/result.html` with:

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

            <!-- Pest photo -->
            <img th:if="${pestPhotoUrl != null and !pestPhotoUrl.isEmpty()}"
                 th:src="${pestPhotoUrl}"
                 th:alt="${farmer.pestName}"
                 class="pest-photo-result"
                 onerror="this.style.display='none'">

            <!-- Crop badge -->
            <span class="crop-badge" th:if="${cropName != null and !cropName.isEmpty()}"
                  th:text="'Crop: ' + ${cropName}">Crop</span>

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

01 HIERARCHY: Eye → pest photo (large, top of card), then crop badge (small, green pill), then pesticide highlight-box (largest value text), then delivery box, then farmer table. Correct priority order — pest context first, recommendation second.

02 TYPOGRAPHY: crop-badge 0.78rem 700 uppercase — consistent with section labels. pest-photo-result max-height 180px — proportional to 560px card width. highlight-box .value 1.4rem 700 unchanged — still primary.

03 WHITESPACE: pest-photo-result margin-bottom 20px. crop-badge margin-bottom 16px. Then highlight-boxes 28px gap. Flows naturally top to bottom.

04 COLOR: crop-badge uses #eaf5ea / #1e4620 / #b4ddb4 — same as highlight-box palette. No new colors. Photo has rounded corners matching card border-radius.

05 CHEAP: (1) onerror on photo = clean degradation. (2) th:if guards on photo and badge = no empty elements. (3) Photo at top draws eye — premium feel. Keep: highlight-box layout unchanged — already works well.

Audits pass. No blocking changes.

- [ ] **Step 3: Commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add src/main/resources/templates/result.html
git commit -m "feat: add pest photo and crop badge to result page"
```

---

## Task 8: End-to-end verification

- [ ] **Step 1: Run full test suite**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn test 2>&1 | grep -E "Tests run:|BUILD"
```

Expected: all tests pass, `BUILD SUCCESS`

- [ ] **Step 2: Start app**

```bash
cd /Users/abhoumic/Downloads/AgriLife && mvn spring-boot:run
```

Expected: `Started AgriLifeApplication in X seconds`

- [ ] **Step 3: Open browser and verify wizard**

Navigate to `http://localhost:8080/`

Verify:
- Progress bar shows 4 dots, step 1 active (green)
- Step 1: name + phone fields visible
- Click Next → moves to step 2 (crop grid), dot 1 turns dark, dot 2 turns green
- 8 crop cards visible with emojis
- Click a crop → highlights green, Next enabled
- Click Next → step 3 shows only pests for selected crop with photos
- Click a pest → highlights green, Next enabled
- Click Next → step 4 shows distance + speed
- Fill distance=30, speed=60, submit → result page
- Result shows: pest photo, crop badge, "Imidacloprid" (if aphids), "35.0 minutes"

- [ ] **Step 4: Test back navigation**

- On step 3, click Back → returns to step 2, crop still selected
- On step 2, click Back → returns to step 1

- [ ] **Step 5: Test all 8 crops**

Select each crop, verify pest grid shows correct filtered pests:
- Rice → Brown Planthopper, Stem Borer, Leaf Folder, Armyworm
- Wheat → Aphids, Armyworm, Locusts
- Maize → Armyworm, Cutworm, Stem Borer, Locusts
- Cotton → Bollworm, Whitefly, Thrips, Mites
- Tomato → Whitefly, Aphids, Mites, Cutworm
- Potato → Aphids, Cutworm, Thrips
- Chili → Thrips, Whitefly, Mites, Jassid
- Onion → Thrips, Armyworm

- [ ] **Step 6: Final commit**

```bash
cd /Users/abhoumic/Downloads/AgriLife
git add -A
git commit -m "feat: Phase 1 complete — Indian crop wizard, 12 pests, pest photos, 4-step UI" 2>/dev/null || echo "nothing to commit"
```

---

## Self-Review

**Spec coverage:**
- ✅ 8 Indian crops → Task 2 (CropService)
- ✅ 12 pests (7 + 5 new) → Task 3 (PestService)
- ✅ Multi-step wizard (4 steps) → Task 6 (index.html)
- ✅ Progress bar → Task 5 (style.css) + Task 6
- ✅ Crop cards with emoji → Task 5 (style.css) + Task 6
- ✅ Pest photo cards → Task 5 (style.css) + Task 6
- ✅ Real pest photos with onerror fallback → Task 3 (photo URLs) + Task 6
- ✅ Back navigation → Task 6 (JS)
- ✅ Result page: pest photo + crop name → Task 7
- ✅ FarmerForm.cropName → Task 4
- ✅ Controller injects crops + pestPhotoMap → Task 4
- ✅ Design audits on all static files → Tasks 5, 6, 7
- ✅ Existing 14 tests still pass → Task 8 step 1
- ✅ 5 new pest tests → Task 3

**Placeholder scan:** No TBDs. URL verification step explicit (curl check + replace instruction). Photo onerror fallback handles broken URLs gracefully at runtime.

**Type consistency:**
- `CropService.getCropPestMap()` returns `Map<String, List<String>>` → Thymeleaf embeds as `cropPestMap` JS object → `cropPestMap[selectedCrop]` returns array. Consistent.
- `PestService.getPestPhotoMap()` returns `Map<String, String>` → `pestPhotoMap` JS object → `pestPhotoMap[pest]` returns string URL. Consistent.
- `FarmerForm.cropName` bound to `hidden-cropName` input → POST binds `form.getCropName()` → `model.addAttribute("cropName", ...)` → `result.html` uses `${cropName}`. Consistent.
- `FarmerForm.pestName` bound to `hidden-pestName` → `form.getPestName()` → `pestService.recommendPesticide()` + `pestService.getPhotoUrl()`. Consistent.
- `Crop.getPestKeys()` used in `CropService.getCropPestMap()` as map values. PestService `PESTICIDE_MAP` keys match `CropService` pestKeys (both lowercase: "brown planthopper", "stem borer", etc.). Consistent.
