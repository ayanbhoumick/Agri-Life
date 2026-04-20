package agrilife;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_records")
public class ReportRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String farmerName;
    private String phone;
    private String cropName;
    private String pestName;
    private String pesticide;
    private double deliveryTime;
    private double distance;
    private double speed;
    private String location;
    private LocalDateTime createdAt;

    protected ReportRecord() {}

    public ReportRecord(String farmerName, String phone, String cropName, String pestName,
                        String pesticide, double deliveryTime, double distance, double speed,
                        String location) {
        this.farmerName = farmerName;
        this.phone = phone;
        this.cropName = cropName;
        this.pestName = pestName;
        this.pesticide = pesticide;
        this.deliveryTime = deliveryTime;
        this.distance = distance;
        this.speed = speed;
        this.location = location;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getFarmerName() { return farmerName; }
    public String getPhone() { return phone; }
    public String getCropName() { return cropName; }
    public String getPestName() { return pestName; }
    public String getPesticide() { return pesticide; }
    public double getDeliveryTime() { return deliveryTime; }
    public double getDistance() { return distance; }
    public double getSpeed() { return speed; }
    public String getLocation() { return location; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
