package agrilife;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PestServiceTest {

    private final PestService service = new PestService();

    @Test
    void aphids_returns_imidacloprid() {
        assertEquals("Imidacloprid", service.recommendPesticide("Aphids"));
    }

    @Test
    void armyworm_returns_spinosad() {
        assertEquals("Spinosad", service.recommendPesticide("armyworm"));
    }

    @Test
    void whitefly_returns_acetamiprid() {
        assertEquals("Acetamiprid", service.recommendPesticide("Whitefly"));
    }

    @Test
    void thrips_returns_abamectin() {
        assertEquals("Abamectin", service.recommendPesticide("thrips"));
    }

    @Test
    void mites_returns_bifenazate() {
        assertEquals("Bifenazate", service.recommendPesticide("mites"));
    }

    @Test
    void locusts_returns_chlorpyrifos() {
        assertEquals("Chlorpyrifos", service.recommendPesticide("locusts"));
    }

    @Test
    void cutworm_returns_lambda_cyhalothrin() {
        assertEquals("Lambda-cyhalothrin", service.recommendPesticide("cutworm"));
    }

    @Test
    void unknown_pest_returns_consult_message() {
        String result = service.recommendPesticide("dragon");
        assertTrue(result.contains("Unknown pest"));
    }

    @Test
    void null_pest_returns_no_info_message() {
        String result = service.recommendPesticide(null);
        assertTrue(result.contains("No pest information"));
    }
}
