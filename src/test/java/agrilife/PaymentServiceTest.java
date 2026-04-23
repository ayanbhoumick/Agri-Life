package agrilife;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PaymentServiceTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(paymentRepository);
    }

    // --- Price map ---

    @Test
    void price_imidacloprid_is_399() {
        assertEquals(399.0, service.getPesticidePrice("Imidacloprid"), 0.001);
    }

    @Test
    void price_spinosad_is_799() {
        assertEquals(799.0, service.getPesticidePrice("Spinosad"), 0.001);
    }

    @Test
    void price_acetamiprid_is_325() {
        assertEquals(325.0, service.getPesticidePrice("Acetamiprid"), 0.001);
    }

    @Test
    void price_abamectin_is_499() {
        assertEquals(499.0, service.getPesticidePrice("Abamectin"), 0.001);
    }

    @Test
    void price_bifenazate_is_699() {
        assertEquals(699.0, service.getPesticidePrice("Bifenazate"), 0.001);
    }

    @Test
    void price_chlorpyrifos_is_425() {
        assertEquals(425.0, service.getPesticidePrice("Chlorpyrifos"), 0.001);
    }

    @Test
    void price_lambda_cyhalothrin_is_375() {
        assertEquals(375.0, service.getPesticidePrice("Lambda-cyhalothrin"), 0.001);
    }

    @Test
    void price_buprofezin_is_549() {
        assertEquals(549.0, service.getPesticidePrice("Buprofezin"), 0.001);
    }

    @Test
    void price_chlorantraniliprole_is_1199() {
        assertEquals(1199.0, service.getPesticidePrice("Chlorantraniliprole"), 0.001);
    }

    @Test
    void price_emamectin_benzoate_is_649() {
        assertEquals(649.0, service.getPesticidePrice("Emamectin Benzoate"), 0.001);
    }

    @Test
    void price_cartap_hydrochloride_is_325() {
        assertEquals(325.0, service.getPesticidePrice("Cartap Hydrochloride"), 0.001);
    }

    @Test
    void price_dimethoate_is_325() {
        assertEquals(325.0, service.getPesticidePrice("Dimethoate"), 0.001);
    }

    @Test
    void price_unknown_pesticide_returns_zero() {
        assertEquals(0.0, service.getPesticidePrice("UnknownChemical"), 0.001);
    }

    // --- Delivery cost ---

    @Test
    void delivery_cost_10km_is_180() {
        assertEquals(180.0, service.calculateDeliveryCost(10.0), 0.001);
    }

    @Test
    void delivery_cost_25km_is_450() {
        assertEquals(450.0, service.calculateDeliveryCost(25.0), 0.001);
    }

    @Test
    void delivery_cost_zero_distance_is_zero() {
        assertEquals(0.0, service.calculateDeliveryCost(0.0), 0.001);
    }

    // --- Card masking ---

    @Test
    void maskCard_returns_last_four_with_asterisks() {
        assertEquals("**** 4242", service.maskCard("1234567890124242"));
    }

    @Test
    void maskCard_returns_last_four_when_short_input() {
        assertEquals("**** 99", service.maskCard("99"));
    }

    // --- processPayment ---

    @Test
    void processPayment_saves_record_to_db() {
        service.processPayment(1L, "Ravi Kumar", "Ravi Kumar", "4111111111111234",
                "Imidacloprid", 20.0);
        List<PaymentRecord> all = paymentRepository.findAll();
        assertEquals(1, all.size());
    }

    @Test
    void processPayment_stores_masked_card() {
        service.processPayment(1L, "Ravi Kumar", "Ravi Kumar", "4111111111111234",
                "Imidacloprid", 20.0);
        PaymentRecord saved = paymentRepository.findAll().get(0);
        assertEquals("**** 1234", saved.getMaskedCard());
    }

    @Test
    void processPayment_calculates_correct_total() {
        // Imidacloprid=399, distance=10 → deliveryCost=180 → total=579
        service.processPayment(1L, "Ravi Kumar", "Ravi Kumar", "4111111111111234",
                "Imidacloprid", 10.0);
        PaymentRecord saved = paymentRepository.findAll().get(0);
        assertEquals(579.0, saved.getTotal(), 0.001);
    }

    @Test
    void processPayment_status_is_PAID() {
        service.processPayment(1L, "Ravi Kumar", "Ravi Kumar", "4111111111111234",
                "Imidacloprid", 10.0);
        PaymentRecord saved = paymentRepository.findAll().get(0);
        assertEquals("PAID", saved.getStatus());
    }

    // --- getAllPayments ---

    @Test
    void getAllPayments_returns_newest_first() throws InterruptedException {
        service.processPayment(1L, "First", "First", "0000000000001111", "Imidacloprid", 10.0);
        Thread.sleep(20);
        service.processPayment(2L, "Second", "Second", "0000000000002222", "Spinosad", 5.0);
        List<PaymentRecord> all = service.getAllPayments();
        assertEquals("Second", all.get(0).getFarmerName());
        assertEquals("First", all.get(1).getFarmerName());
    }
}
