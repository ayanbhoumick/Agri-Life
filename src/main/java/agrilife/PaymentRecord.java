package agrilife;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_records")
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reportId;
    private String farmerName;
    private String cardholderName;
    private String maskedCard;
    private String pesticide;
    private double pesticidePrice;
    private double deliveryCost;
    private double total;
    private LocalDateTime paidAt;
    private String status;

    protected PaymentRecord() {}

    public PaymentRecord(Long reportId, String farmerName, String cardholderName,
                         String maskedCard, String pesticide, double pesticidePrice,
                         double deliveryCost) {
        this.reportId = reportId;
        this.farmerName = farmerName;
        this.cardholderName = cardholderName;
        this.maskedCard = maskedCard;
        this.pesticide = pesticide;
        this.pesticidePrice = pesticidePrice;
        this.deliveryCost = deliveryCost;
        this.total = pesticidePrice + deliveryCost;
        this.paidAt = LocalDateTime.now();
        this.status = "PAID";
    }

    public Long getId() { return id; }
    public Long getReportId() { return reportId; }
    public String getFarmerName() { return farmerName; }
    public String getCardholderName() { return cardholderName; }
    public String getMaskedCard() { return maskedCard; }
    public String getPesticide() { return pesticide; }
    public double getPesticidePrice() { return pesticidePrice; }
    public double getDeliveryCost() { return deliveryCost; }
    public double getTotal() { return total; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public String getStatus() { return status; }
}
