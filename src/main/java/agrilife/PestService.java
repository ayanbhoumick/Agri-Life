package agrilife;

import org.springframework.stereotype.Service;

@Service
public class PestService {

    public String recommendPesticide(String pestName) {
        if (pestName == null || pestName.trim().isEmpty()) {
            return "No pest information provided. Please consult an expert.";
        }

        String pest = pestName.trim().toLowerCase();

        if (pest.equals("aphids") || pest.equals("aphid")) {
            return "Imidacloprid";
        } else if (pest.equals("armyworm") || pest.equals("army worm")) {
            return "Spinosad";
        } else if (pest.equals("whitefly") || pest.equals("white fly")) {
            return "Acetamiprid";
        } else if (pest.equals("thrips") || pest.equals("thrip")) {
            return "Abamectin";
        } else if (pest.equals("mites") || pest.equals("spider mites")) {
            return "Bifenazate";
        } else if (pest.equals("locusts") || pest.equals("locust")) {
            return "Chlorpyrifos";
        } else if (pest.equals("cutworm") || pest.equals("cut worm")) {
            return "Lambda-cyhalothrin";
        } else {
            return "Unknown pest. Please consult an agricultural expert.";
        }
    }
}
