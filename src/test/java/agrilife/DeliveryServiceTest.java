package agrilife;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeliveryServiceTest {

    private final DeliveryService service = new DeliveryService();

    @Test
    void calculates_delivery_time_correctly() {
        double result = service.calculateDeliveryTime(30, 60);
        assertEquals(35.0, result, 0.001);
    }

    @Test
    void zero_distance_returns_verification_time_only() {
        double result = service.calculateDeliveryTime(0, 50);
        assertEquals(5.0, result, 0.001);
    }

    @Test
    void zero_speed_throws_illegal_argument() {
        assertThrows(IllegalArgumentException.class,
            () -> service.calculateDeliveryTime(10, 0));
    }

    @Test
    void negative_speed_throws_illegal_argument() {
        assertThrows(IllegalArgumentException.class,
            () -> service.calculateDeliveryTime(10, -5));
    }

    @Test
    void negative_distance_throws_illegal_argument() {
        assertThrows(IllegalArgumentException.class,
            () -> service.calculateDeliveryTime(-1, 60));
    }
}
