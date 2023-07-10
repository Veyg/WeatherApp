package com.weatherapp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WeatherApp extends Application implements Initializable {
    private static String API_KEY;
    
    @FXML
    private TextField cityInput;
    
    @FXML
    private Button fetchButton;
    
    @FXML
    private Label temperatureOutput;
    @FXML
    private Label humidityOutput;
    @FXML
    private Label pressureOutput;
    @FXML
    private Label windSpeedOutput;
    @FXML
    private Label sunriseOutput;
    @FXML
    private Label sunsetOutput;
    @FXML
    private Label weatherDescriptionOutput;
    @FXML
    private ProgressBar progressBar;

    @Override
    public void start(Stage primaryStage) throws Exception {
        loadProperties();
        Parent root = FXMLLoader.load(getClass().getResource("/weather_app.fxml"));
        Scene scene = new Scene(root, 400, 200);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        Image icon = new Image(getClass().getResourceAsStream("/WeatherApp-logos.jpeg"));
        primaryStage.getIcons().add(icon); 
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // fetchButton logic moved to the fetchWeather method
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void loadProperties() throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = WeatherApp.class.getClassLoader().getResourceAsStream("config.properties");
        if (inputStream != null) {
            properties.load(inputStream);
            API_KEY = properties.getProperty("openweathermap.api.key");
        } else {
            throw new RuntimeException("config.properties not found in the classpath");
        }
    }

    public void getWeatherByCityName(String city) throws IOException {
        String url = String.format("http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=%s", city, API_KEY);
        getWeather(url);
    }

    public static void getWeatherByCoordinates(String lat, String lon) throws IOException {
        String url = String.format("http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric&appid=%s", lat, lon, API_KEY);
        getWeather(url);
    }

    public void getWeather(String url) throws IOException {
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

            JSONObject main = (JSONObject) jsonObject.get("main");
            JSONObject wind = (JSONObject) jsonObject.get("wind");
            JSONObject sys = (JSONObject) jsonObject.get("sys");
            JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
            JSONObject weather = (JSONObject) weatherArray.get(0);

            // Update the labels with the fetched information
            temperatureOutput.setText("Temperature: " + main.get("temp").toString());
            humidityOutput.setText("Humidity: " + main.get("humidity").toString());
            pressureOutput.setText("Pressure: " + main.get("pressure").toString());
            windSpeedOutput.setText("Wind Speed: " + wind.get("speed").toString());
            sunriseOutput.setText("Sunrise: " + sys.get("sunrise").toString());
            sunsetOutput.setText("Sunset: " + sys.get("sunset").toString());
            weatherDescriptionOutput.setText("Weather: " + weather.get("description").toString());

        } catch (ParseException e) {
            System.out.println("An error occurred while parsing the API response. Please try again.");
            e.printStackTrace();
        } finally {
            response.close();
        }
    }

    public void fetchWeather(ActionEvent event) {
        Button b = (Button) event.getSource();
        b.setDisable(true);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            b.setDisable(false);
            b.setText("Get Weather");
        }));

        timeline.setCycleCount(1);
        timeline.play();

        // your current logic goes here
        try {
            progressBar.setVisible(true);
            getWeatherByCityName(cityInput.getText());
            progressBar.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
            progressBar.setVisible(false);
        }
    }
}
