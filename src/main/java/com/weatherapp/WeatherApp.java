package com.weatherapp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class WeatherApp {
    private static String API_KEY;

    public static void main(String[] args) throws IOException {
        loadProperties();
        String city = "London,uk";
        getWeather(city);
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

    public static void getWeather(String city) throws IOException {
        String url = String.format("http://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s", city, API_KEY);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);
        CloseableHttpResponse response = client.execute(request);

        try {
            System.out.println(EntityUtils.toString(response.getEntity()));
        } finally {
            response.close();
        }
    }
}
