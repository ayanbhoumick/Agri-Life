package agrilife;

import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository repository;

    public ReportService(ReportRepository repository) {
        this.repository = repository;
    }

    public void save(FarmerForm form, String pesticide, double deliveryTime, double effectiveSpeed) {
        ReportRecord record = new ReportRecord(
            form.getName(), form.getPhone(), form.getCropName(), form.getPestName(),
            pesticide, deliveryTime, form.getDistance(), effectiveSpeed, form.getLocation()
        );
        repository.save(record);
    }

    public List<ReportRecord> getAllReports() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public long getTotalCount() {
        return repository.count();
    }

    public String getMostCommonPest() {
        List<ReportRecord> records = repository.findAll();
        if (records.isEmpty()) return "None";
        return records.stream()
            .collect(Collectors.groupingBy(ReportRecord::getPestName, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("None");
    }

    public double getAvgDeliveryTime() {
        List<ReportRecord> records = repository.findAll();
        if (records.isEmpty()) return 0.0;
        return records.stream()
            .mapToDouble(ReportRecord::getDeliveryTime)
            .average()
            .orElse(0.0);
    }
}
