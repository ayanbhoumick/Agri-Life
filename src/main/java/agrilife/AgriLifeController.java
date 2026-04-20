package agrilife;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AgriLifeController {

    private final PestService pestService;
    private final DeliveryService deliveryService;
    private final CropService cropService;
    private final ReportService reportService;

    public AgriLifeController(PestService pestService, DeliveryService deliveryService,
                               CropService cropService, ReportService reportService) {
        this.pestService = pestService;
        this.deliveryService = deliveryService;
        this.cropService = cropService;
        this.reportService = reportService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("farmerForm", new FarmerForm());
        model.addAttribute("crops", cropService.getAllCrops());
        model.addAttribute("cropPestMap", cropService.getCropPestMap());
        model.addAttribute("pestPhotoMap", pestService.getPestPhotoMap());
        return "index";
    }

    @PostMapping("/recommend")
    public String recommend(@ModelAttribute FarmerForm form, Model model) {
        Farmer farmer = new Farmer(form.getName(), form.getPhone(), form.getPestName());
        String recommendation = pestService.recommendPesticide(farmer.getPestName());
        String pestPhotoUrl = pestService.getPhotoUrl(farmer.getPestName());

        double deliveryTime;
        double effectiveSpeed = form.getSpeed();
        String deliveryError = null;
        try {
            deliveryTime = deliveryService.calculateDeliveryTime(form.getDistance(), form.getSpeed());
        } catch (IllegalArgumentException e) {
            effectiveSpeed = 30;
            deliveryTime = deliveryService.calculateDeliveryTime(form.getDistance(), effectiveSpeed);
            deliveryError = "Invalid speed — defaulted to 30 km/h.";
        }

        reportService.save(form, recommendation, deliveryTime, effectiveSpeed);

        model.addAttribute("farmer", farmer);
        model.addAttribute("cropName", form.getCropName());
        model.addAttribute("recommendation", recommendation);
        model.addAttribute("pestPhotoUrl", pestPhotoUrl);
        model.addAttribute("deliveryTime", String.format("%.1f", deliveryTime));
        model.addAttribute("distance", form.getDistance());
        model.addAttribute("speed", effectiveSpeed);
        model.addAttribute("deliveryError", deliveryError);
        return "result";
    }
}
