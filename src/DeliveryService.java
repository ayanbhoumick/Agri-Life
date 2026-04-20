package agrilife;

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

    public void displayDeliveryEstimate(double distanceKm, double speedKmh, double estimatedMinutes) {
        System.out.println("========================================");
        System.out.println("       DELIVERY ESTIMATION              ");
        System.out.println("========================================");
        System.out.printf("  Distance        : %.1f km%n", distanceKm);
        System.out.printf("  Speed           : %.1f km/h%n", speedKmh);
        System.out.printf("  Verification    : %d min%n", VERIFICATION_TIME);
        System.out.printf("  Estimated Time  : %.1f minutes%n", estimatedMinutes);
        System.out.println("========================================");
    }
}