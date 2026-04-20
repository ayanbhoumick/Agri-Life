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
