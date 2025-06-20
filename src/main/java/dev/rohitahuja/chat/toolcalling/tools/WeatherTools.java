package dev.rohitahuja.chat.toolcalling.tools;

import dev.rohitahuja.chat.toolcalling.WeatherConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.web.client.RestClient;

public class WeatherTools {

    private static final Logger _log = LoggerFactory.getLogger(WeatherTools.class);
    private final WeatherConfigProperties weatherConfigProperties;
    private final RestClient restClient;

    public WeatherTools(WeatherConfigProperties weatherConfigProperties) {
        this.weatherConfigProperties = weatherConfigProperties;
        _log.debug("Weather API URL: {}", weatherConfigProperties.apiUrl());
        _log.debug("Weather API Key: {}", weatherConfigProperties.apiKey());
        this.restClient = RestClient.create(weatherConfigProperties.apiUrl());
    }

    @Tool(description = "Get the current weather conditions for the given city.")
    public Response getWeather(Request request) {
        _log.info("Weather Request: {}", request);
        Response response = restClient.get()
                .uri("/current.json?key={key}&q={q}", weatherConfigProperties.apiKey(), request.city())
                .retrieve()
                .body(Response.class);
        _log.info("Weather API Response: {}", response);
        return response;
    }

    // mapping the response of the Weather API to records.
    public record Request(String city) {}
    public record Response(Location location,Current current) {}
    public record Location(String name, String region, String country, Long lat, Long lon, String local_time){}
    public record Current(String temp_c, Condition condition, String wind_kph, String humidity) {}
    public record Condition(String text){}
}
