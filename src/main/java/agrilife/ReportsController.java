package agrilife;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportsController {

    private final ReportService reportService;

    public ReportsController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("reports", reportService.getAllReports());
        return "reports";
    }
}
