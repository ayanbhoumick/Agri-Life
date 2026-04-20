package agrilife;

public class Farmer {

    private String name;
    private String phoneNumber;
    private String pestName;

    public Farmer(String name, String phoneNumber, String pestName) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.pestName = pestName;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPestName() {
        return pestName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPestName(String pestName) {
        this.pestName = pestName;
    }

    public void displayDetails() {
        System.out.println("========================================");
        System.out.println("          FARMER DETAILS                ");
        System.out.println("========================================");
        System.out.println("  Name         : " + name);
        System.out.println("  Phone Number : " + phoneNumber);
        System.out.println("  Pest Reported: " + pestName);
        System.out.println("========================================");
    }
}
