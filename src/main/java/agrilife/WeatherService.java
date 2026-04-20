package agrilife;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WeatherService {

    private final RestTemplate restTemplate = new RestTemplate();

    public WeatherData getWeather(String city) {
        try {
            String encoded = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name="
                + encoded + "&count=1&language=en&format=json";

            @SuppressWarnings("unchecked")
            Map<String, Object> geoResp = restTemplate.getForObject(geoUrl, Map.class);
            if (geoResp == null) return null;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>) geoResp.get("results");
            if (results == null || results.isEmpty()) return null;

            Map<String, Object> place = results.get(0);
            Number latNum = (Number) place.get("latitude");
            Number lngNum = (Number) place.get("longitude");
            if (latNum == null || lngNum == null) return null;
            double lat = latNum.doubleValue();
            double lng = lngNum.doubleValue();
            String resolvedName = (String) place.get("name");
            if (resolvedName == null) resolvedName = city;

            String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + lat
                + "&longitude=" + lng
                + "&current=temperature_2m,precipitation,weathercode&timezone=auto";

            @SuppressWarnings("unchecked")
            Map<String, Object> weatherResp = restTemplate.getForObject(weatherUrl, Map.class);
            if (weatherResp == null) return null;

            @SuppressWarnings("unchecked")
            Map<String, Object> current = (Map<String, Object>) weatherResp.get("current");
            if (current == null) return null;

            Number tempNum = (Number) current.get("temperature_2m");
            Number precipNum = (Number) current.get("precipitation");
            Number codeNum = (Number) current.get("weathercode");
            if (tempNum == null || precipNum == null || codeNum == null) return null;
            double temperature = tempNum.doubleValue();
            double precipitation = precipNum.doubleValue();
            int weatherCode = codeNum.intValue();
            List<String> riskPests = deriveRiskPests(temperature, precipitation);

            return new WeatherData(resolvedName, temperature, precipitation, weatherCode, riskPests);
        } catch (Exception e) {
            return null;
        }
    }

    List<String> deriveRiskPests(double temperature, double precipitation) {
        List<String> risks = new ArrayList<>();
        if (temperature >= 35 && precipitation < 2) {
            risks.add("Locusts");
            risks.add("Whitefly");
            risks.add("Aphids");
        }
        if (precipitation >= 5 && temperature >= 20) {
            risks.add("Brown Planthopper");
            risks.add("Leaf Folder");
            risks.add("Stem Borer");
        }
        if (temperature >= 20 && temperature < 35 && precipitation < 2) {
            risks.add("Aphids");
            risks.add("Mites");
            risks.add("Thrips");
        }
        if (temperature < 20 && precipitation >= 3) {
            risks.add("Armyworm");
            risks.add("Cutworm");
        }
        return risks.stream().distinct().collect(Collectors.toList());
    }
}
