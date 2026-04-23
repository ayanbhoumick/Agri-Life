package agrilife;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentController {

    private final ReportRepository reportRepository;
    private final PaymentService paymentService;

    public PaymentController(ReportRepository reportRepository, PaymentService paymentService) {
        this.reportRepository = reportRepository;
        this.paymentService = paymentService;
    }

    @GetMapping("/checkout")
    public String checkout(@RequestParam Long reportId,
                           @RequestParam(required = false) Boolean success,
                           Model model) {
        ReportRecord report = reportRepository.findById(reportId).orElse(null);
        if (report == null) {
            return "redirect:/";
        }
        double pesticidePrice = paymentService.getPesticidePrice(report.getPesticide());
        double deliveryCost   = paymentService.calculateDeliveryCost(report.getDistance());
        double total          = pesticidePrice + deliveryCost;

        model.addAttribute("report", report);
        model.addAttribute("pesticidePrice", pesticidePrice);
        model.addAttribute("deliveryCost", deliveryCost);
        model.addAttribute("total", total);
        model.addAttribute("success", Boolean.TRUE.equals(success));
        return "checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(@RequestParam Long reportId,
                                  @RequestParam String cardholderName,
                                  @RequestParam String cardNumber,
                                  @RequestParam String expiry,
                                  @RequestParam String cvv) {
        ReportRecord report = reportRepository.findById(reportId).orElse(null);
        if (report == null) {
            return "redirect:/";
        }
        paymentService.processPayment(
            reportId,
            report.getFarmerName(),
            cardholderName,
            cardNumber,
            report.getPesticide(),
            report.getDistance()
        );
        return "redirect:/checkout?reportId=" + reportId + "&success=true";
    }

    @GetMapping("/payments")
    public String payments(Model model) {
        model.addAttribute("payments", paymentService.getAllPayments());
        return "payments";
    }
}
