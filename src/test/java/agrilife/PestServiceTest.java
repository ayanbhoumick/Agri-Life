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
}
