package agrilife;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ReportServiceTest {

    @Autowired
    private ReportRepository repository;

    private ReportService service;

    @BeforeEach
    void setUp() {
        service = new ReportService(repository);
    }

    private FarmerForm makeForm(String name, String crop, String pest) {
        FarmerForm f = new FarmerForm();
        f.setName(name);
        f.setPhone("9876543210");
        f.setCropName(crop);
        f.setPestName(pest);
        f.setDistance(20.0);
        f.setSpeed(40.0);
        return f;
    }

    @Test
    void save_persists_one_record() {
        service.save(makeForm("Rajesh", "Rice", "brown planthopper"), "Buprofezin", 35.0, 40.0);
        assertEquals(1, service.getAllReports().size());
    }

    @Test
    void save_stores_all_fields_correctly() {
        FarmerForm form = makeForm("Priya", "Wheat", "aphids");
        service.save(form, "Imidacloprid", 22.5, 50.0);
        ReportRecord saved = service.getAllReports().get(0);
        assertEquals("Priya", saved.getFarmerName());
        assertEquals("9876543210", saved.getPhone());
        assertEquals("Wheat", saved.getCropName());
        assertEquals("aphids", saved.getPestName());
        assertEquals("Imidacloprid", saved.getPesticide());
        assertEquals(22.5, saved.getDeliveryTime());
        assertEquals(20.0, saved.getDistance());
        assertEquals(50.0, saved.getSpeed());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void getAllReports_returns_newest_first() throws InterruptedException {
        service.save(makeForm("First", "Rice", "aphids"), "Imidacloprid", 20.0, 60.0);
        Thread.sleep(20);
        service.save(makeForm("Second", "Cotton", "bollworm"), "Emamectin Benzoate", 30.0, 60.0);
        List<ReportRecord> reports = service.getAllReports();
        assertEquals("Second", reports.get(0).getFarmerName());
        assertEquals("First", reports.get(1).getFarmerName());
    }

    @Test
    void empty_db_returns_empty_list() {
        assertTrue(service.getAllReports().isEmpty());
    }
}
