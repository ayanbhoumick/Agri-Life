package agrilife;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    private static final Map<String, Double> PRICE_MAP = new HashMap<>();
    private static final double DELIVERY_RATE = 18.0;

    static {
        PRICE_MAP.put("Imidacloprid",        399.0);
        PRICE_MAP.put("Spinosad",            799.0);
        PRICE_MAP.put("Acetamiprid",         325.0);
        PRICE_MAP.put("Abamectin",           499.0);
        PRICE_MAP.put("Bifenazate",          699.0);
        PRICE_MAP.put("Chlorpyrifos",        425.0);
        PRICE_MAP.put("Lambda-cyhalothrin",  375.0);
        PRICE_MAP.put("Buprofezin",          549.0);
        PRICE_MAP.put("Chlorantraniliprole", 1199.0);
        PRICE_MAP.put("Emamectin Benzoate",  649.0);
        PRICE_MAP.put("Cartap Hydrochloride",325.0);
        PRICE_MAP.put("Dimethoate",          325.0);
    }

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public double getPesticidePrice(String pesticide) {
        return PRICE_MAP.getOrDefault(pesticide, 0.0);
    }

    public double calculateDeliveryCost(double distance) {
        return distance * DELIVERY_RATE;
    }

    public String maskCard(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "**** " + (cardNumber == null ? "" : cardNumber);
        }
        return "**** " + cardNumber.substring(cardNumber.length() - 4);
    }

    public void processPayment(Long reportId, String farmerName, String cardholderName,
                               String cardNumber, String pesticide, double distance) {
        double pesticidePrice = getPesticidePrice(pesticide);
        double deliveryCost = calculateDeliveryCost(distance);
        String masked = maskCard(cardNumber);
        PaymentRecord record = new PaymentRecord(reportId, farmerName, cardholderName,
                masked, pesticide, pesticidePrice, deliveryCost);
        paymentRepository.save(record);
    }

    public List<PaymentRecord> getAllPayments() {
        return paymentRepository.findAllByOrderByPaidAtDesc();
    }
}
