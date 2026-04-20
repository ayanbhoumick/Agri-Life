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
