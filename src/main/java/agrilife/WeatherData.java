package agrilife;

import java.util.List;

public class WeatherData {

    private final String city;
    private final double temperature;
    private final double precipitation;
    private final int weatherCode;
    private final List<String> riskPests;

    public WeatherData(String city, double temperature, double precipitation,
                       int weatherCode, List<String> riskPests) {
        this.city = city;
        this.temperature = temperature;
        this.precipitation = precipitation;
        this.weatherCode = weatherCode;
        this.riskPests = riskPests;
    }

    public String getCity() { return city; }
    public double getTemperature() { return temperature; }
    public double getPrecipitation() { return precipitation; }
    public int getWeatherCode() { return weatherCode; }
    public List<String> getRiskPests() { return riskPests; }
}
