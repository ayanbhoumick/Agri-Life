# AgriLife

A web-based pest management and delivery estimation system for farmers.

## Features

- **Pest identification & pesticide recommendation** — select crop and pest, get the right pesticide instantly
- **Delivery time estimation** — calculates estimated delivery based on distance and transport speed
- **Live weather context** — enter your city to see current temperature, precipitation, and high-risk pests for those conditions
- **Report history** — every submission is saved and viewable at `/reports`
- **App-wide statistics** — total reports, most common pest, and average delivery time shown after each submission

## Tech Stack

- Java 17
- Spring Boot 3.2.5
- Thymeleaf
- Spring Data JPA + H2 (in-memory database)
- Open-Meteo API (free, no key required)
- Maven

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+

### Run

```bash
git clone https://github.com/ayanbhoumick/Agri-Life.git
cd Agri-Life
mvn spring-boot:run
```

Open `http://localhost:8080` in your browser.

### Run Tests

```bash
mvn test
```

41 tests across pest logic, delivery calculation, crop mappings, database persistence, and weather risk rules.

## Usage

1. Open `http://localhost:8080`
2. Fill in your name, phone number, and optionally your city
3. Select your crop and the pest you've identified
4. Submit — get your pesticide recommendation, delivery estimate, and weather-based pest risk card
5. View all past reports at `http://localhost:8080/reports`
