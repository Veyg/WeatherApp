package com.weatherapp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WeatherApp extends Application {
    private static String API_KEY;

    @Override
    public void start(Stage primaryStage) throws Exception {
        loadProperties();
        VBox vbox = new VBox();
        Scene scene = new Scene(vbox, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
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

    public static void getWeatherByCityName(String city) throws IOException {
        String url = String.format("http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=%s", city, API_KEY);
        getWeather(url);
    }

    public static void getWeatherByCoordinates(String lat, String lon) throws IOException {
        String url = String.format("http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric&appid=%s", lat, lon, API_KEY);
        getWeather(url);
    }

    public static void getWeather(String url) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);
        CloseableHttpResponse response = client.execute(request);

        JSONParser parser = new JSONParser();

        try {
            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("An error occurred: " + response.getStatusLine().getReasonPhrase());
                return;
            }

            String responseBody = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = (JSONObject) parser.parse(responseBody);

            String cityName = (String) jsonObject.get("name");
            JSONObject main = (JSONObject) jsonObject.get("main");
            Double temperature = ((Number) main.get("temp")).doubleValue();
            Double feelsLike = ((Number) main.get("feels_like")).doubleValue();
            Long humidity = ((Number) main.get("humidity")).longValue();
            Long pressure = ((Number) main.get("pressure")).longValue();

            JSONObject wind = (JSONObject) jsonObject.get("wind");
            Double windSpeed = ((Number) wind.get("speed")).doubleValue();

            JSONObject sys = (JSONObject) jsonObject.get("sys");
            Long sunrise = ((Number) sys.get("sunrise")).longValue();
            Long sunset = ((Number) sys.get("sunset")).longValue();

            JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
            JSONObject weather = (JSONObject) weatherArray.get(0);
            String weatherDescription = (String) weather.get("description");

            System.out.println("City: " + cityName);
            System.out.println("Temperature: " + temperature);
            System.out.println("Feels Like: " + feelsLike);
            System.out.println("Humidity: " + humidity);
            System.out.println("Pressure: " + pressure);
            System.out.println("Wind Speed: " + windSpeed);
            System.out.println("Sunrise: " + sunrise);
            System.out.println("Sunset: " + sunset);
            System.out.println("Weather: " + weatherDescription);

        } catch (ParseException e) {
            System.out.println("An error occurred while parsing the API response. Please try again.");
            e.printStackTrace();
        } finally {
            response.close();
        }
    }
}
