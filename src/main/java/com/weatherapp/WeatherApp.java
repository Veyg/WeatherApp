package com.weatherapp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WeatherApp {
    private static String API_KEY;

    public static void main(String[] args) throws IOException, ParseException {
        loadProperties();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter a city name (or 'quit' to exit): ");
            String city = scanner.nextLine();

            if (city.equalsIgnoreCase("quit")) {
                break;
            }
            try {
                getWeather(city);
            } catch (IOException | ParseException e) {
                System.out.println("Unable to retrieve weather data. Check if the city is correct.");
            }
        }
        scanner.close();
    }

    private static void loadProperties() throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = WeatherApp.class.getClassLoader().getResourceAsStream("config.properties");
        if (inputStream != null) {
            properties.load(inputStream);
            API_KEY = properties.getProperty("openweathermap.api.key");
        } else {
            throw new RuntimeException("config.properties not found in the classpath");
        }
    }

    public static void getWeather(String city) throws IOException, ParseException {
        String url = String.format("http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=%s", city, API_KEY);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);
        CloseableHttpResponse response = client.execute(request);

        JSONParser parser = new JSONParser();

        try {
            String responseBody = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = (JSONObject) parser.parse(responseBody);
            
            String cityName = (String) jsonObject.get("name");
            JSONObject main = (JSONObject) jsonObject.get("main");
            Double temperature = (Double) main.get("temp");
            Double feelsLike = (Double) main.get("feels_like");
            Long humidity = (Long) main.get("humidity");
            Long pressure = (Long) main.get("pressure");

            System.out.println("City: " + cityName);
            System.out.println("Temperature: " + temperature);
            System.out.println("Feels Like: " + feelsLike);
            System.out.println("Humidity: " + humidity);
            System.out.println("Pressure: " + pressure);
        } finally {
            response.close();
        }
    }
}
