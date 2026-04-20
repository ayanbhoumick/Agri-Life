package agrilife;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReportService {

    private final ReportRepository repository;

    public ReportService(ReportRepository repository) {
        this.repository = repository;
    }

    public void save(FarmerForm form, String pesticide, double deliveryTime, double effectiveSpeed) {
        ReportRecord record = new ReportRecord(
            form.getName(), form.getPhone(), form.getCropName(), form.getPestName(),
            pesticide, deliveryTime, form.getDistance(), effectiveSpeed
        );
        repository.save(record);
    }

    public List<ReportRecord> getAllReports() {
        return repository.findAllByOrderByCreatedAtDesc();
    }
}
