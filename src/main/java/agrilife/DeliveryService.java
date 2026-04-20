package agrilife;

import org.springframework.stereotype.Service;

@Service
public class DeliveryService {

    private static final int VERIFICATION_TIME = 5;

    public double calculateDeliveryTime(double distanceKm, double speedKmh) {
        if (speedKmh <= 0) {
            throw new IllegalArgumentException("Speed must be greater than zero.");
        }
        if (distanceKm < 0) {
            throw new IllegalArgumentException("Distance cannot be negative.");
        }

        double travelTime = (distanceKm * 60) / speedKmh;
        return travelTime + VERIFICATION_TIME;
    }
}
