package agrilife;

import java.util.Scanner;

public class MainApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("      WELCOME TO AGRI-LIFE SYSTEM       ");
        System.out.println("========================================");
        System.out.println();

        System.out.println("Please enter your details:");
        System.out.print("  Farmer Name    : ");
        String name = scanner.nextLine().trim();

        System.out.print("  Phone Number   : ");
        String phone = scanner.nextLine().trim();

        System.out.print("  Pest Name      : ");
        String pestName = scanner.nextLine().trim();

        Farmer farmer = new Farmer(name, phone, pestName);

        System.out.println();

        double distance = 0;
        double speed = 0;

        try {
            System.out.print("  Distance to farm (km) : ");
            distance = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("  Delivery speed (km/h)  : ");
            speed = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("\n  [Error] Invalid number. Using defaults: 10 km at 30 km/h.");
            distance = 10;
            speed = 30;
        }

        System.out.println();

        PestService pestService = new PestService();
        String recommendation = pestService.recommendPesticide(farmer.getPestName());

        DeliveryService deliveryService = new DeliveryService();
        double estimatedTime;

        try {
            estimatedTime = deliveryService.calculateDeliveryTime(distance, speed);
        } catch (IllegalArgumentException e) {
            System.out.println("  [Error] " + e.getMessage() + " Using default speed of 30 km/h.");
            estimatedTime = deliveryService.calculateDeliveryTime(distance, 30);
        }

        System.out.println("\n============ AGRI-LIFE REPORT ============\n");
        farmer.displayDetails();
        System.out.println();
        pestService.displayRecommendation(farmer.getPestName(), recommendation);
        System.out.println();
        deliveryService.displayDeliveryEstimate(distance, speed, estimatedTime);

        System.out.println("\n  Thank you for using Agri-Life System!");
        System.out.println("==========================================");

        scanner.close();
    }
}