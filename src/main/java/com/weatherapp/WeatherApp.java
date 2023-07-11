package com.weatherapp;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.ResourceBundle;

public class WeatherApp extends Application implements Initializable {
    // API key for OpenWeatherMap
    private static String API_KEY;

    @FXML private TextField cityInput;
    @FXML private Button fetchButton;
    @FXML private Label temperatureOutput;
    @FXML private Label humidityOutput;
    @FXML private Label pressureOutput;
    @FXML private Label windSpeedOutput;
    @FXML private Label sunriseOutput;
    @FXML private Label sunsetOutput;
    @FXML private Label weatherDescriptionOutput;
    @FXML private ProgressBar progressBar;
    @FXML private ImageView weatherIcon;

    @Override
    public void start(Stage primaryStage) throws Exception {
        loadProperties();
        Parent root = FXMLLoader.load(getClass().getResource("/weather_app.fxml"));
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        Image icon = new Image(getClass().getResourceAsStream("/WeatherApp-logos.jpeg"));
        primaryStage.getIcons().add(icon);
        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(350);
        primaryStage.setMaxHeight(400);
        primaryStage.setMaxWidth(350);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
            String iconId = (String) weather.get("icon");

            // Fetch and update the weather information
            updateWeatherInfo(main, wind, sys, weather, iconId);

        } catch (Exception e) {
            System.out.println("An error occurred while parsing the API response. Please try again.");
            e.printStackTrace();
        } finally {
            response.close();
        }
    }

    private void updateWeatherInfo(JSONObject main, JSONObject wind, JSONObject sys, JSONObject weather, String iconId) {
        temperatureOutput.setText(String.format("%s °C", main.get("temp").toString()));
        humidityOutput.setText(String.format("%s %%", main.get("humidity").toString()));
        pressureOutput.setText(String.format("%s hPa", main.get("pressure").toString()));
        windSpeedOutput.setText(String.format("%s m/s", wind.get("speed").toString()));

        // Parsing sunrise and sunset time
        sunriseOutput.setText(parseUnixTime(sys.get("sunrise").toString()));
        sunsetOutput.setText(parseUnixTime(sys.get("sunset").toString()));

        weatherDescriptionOutput.setText(weather.get("description").toString());
        Image image = new Image("http://openweathermap.org/img/wn/" + iconId + ".png");
        weatherIcon.setImage(image);
    }

    public void fetchWeather(ActionEvent event) {
        fetchButton.setDisable(true);
        progressBar.setVisible(true);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            fetchButton.setDisable(false);
            fetchButton.setText("Get Weather");
        }));

        timeline.setCycleCount(1);
        timeline.play();

        try {
            getWeatherByCityName(cityInput.getText());
            progressBar.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
            progressBar.setVisible(false);
        }
    }

    private String parseUnixTime(String unixTime) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(unixTime)), ZoneId.systemDefault());
        return dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }
}
