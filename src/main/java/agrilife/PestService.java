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
            "https://upload.wikimedia.org/wikipedia/commons/9/90/Aphids_May_2010-3.jpg");
        PHOTO_MAP.put("armyworm",
            "https://upload.wikimedia.org/wikipedia/commons/9/9d/Mythimna.unipuncta.01.jpg");
        PHOTO_MAP.put("whitefly",
            "https://upload.wikimedia.org/wikipedia/commons/a/a7/Silverleaf_whitefly.jpg");
        PHOTO_MAP.put("thrips",
            "https://upload.wikimedia.org/wikipedia/commons/8/86/Thrips_on_pepper_flower.jpg");
        PHOTO_MAP.put("mites",
            "https://upload.wikimedia.org/wikipedia/commons/a/a5/Tiny_little_Spider_Mite.jpg");
        PHOTO_MAP.put("locusts",
            "https://upload.wikimedia.org/wikipedia/commons/6/63/Desert_locust_-_W%C3%BCstenheuschrecke_-_Criquet_p%C3%A8lerin_-_Schistocerca_gregaria.jpg");
        PHOTO_MAP.put("cutworm",
            "https://upload.wikimedia.org/wikipedia/commons/d/de/Agrotis_ipsilon_%28black_cutworm%29%2C_side_2014-06-04-19.42.51_ZS_PMax_%2815752867389%29.jpg");
        PHOTO_MAP.put("brown planthopper",
            "https://upload.wikimedia.org/wikipedia/commons/d/d6/Nilaparvata_lugens_-_Brown_planthopper_-_UGA5190055.jpg");
        PHOTO_MAP.put("stem borer",
            "https://upload.wikimedia.org/wikipedia/commons/c/c0/Rice_yellow_stem_borer.jpg");
        PHOTO_MAP.put("bollworm",
            "https://upload.wikimedia.org/wikipedia/commons/b/b8/Cotton_Bollworm_Moth_%28Helicoverpa_armigera%29_%2853172586504%29.jpg");
        PHOTO_MAP.put("leaf folder",
            "https://upload.wikimedia.org/wikipedia/commons/8/83/Cnaphalocrocis_medinalis.jpg");
        PHOTO_MAP.put("jassid",
            "https://upload.wikimedia.org/wikipedia/commons/e/ec/Empoasca_fabae_P1400582a.jpg");
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
