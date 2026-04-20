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
