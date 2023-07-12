package com.weatherapp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
import org.json.simple.parser.ParseException;

public class WeatherApp extends Application implements Initializable {
    private static final String CONFIG_PROPERTIES = "config.properties";
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
    @FXML
    private ImageView weatherIcon;

    public static void main(String[] args) {
        launch(args);
    }

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

    private void loadProperties() throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = WeatherApp.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTIES);
        if (inputStream != null) {
            properties.load(inputStream);
            API_KEY = properties.getProperty("openweathermap.api.key");
        } else {
            throw new RuntimeException(CONFIG_PROPERTIES + " not found in the classpath");
        }
    }

    public void fetchWeather(ActionEvent event) {
        fetchButton.setDisable(true);
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            fetchButton.setDisable(false);
            fetchButton.setText("Get Weather");
        }));
        timeline.setCycleCount(1);
        timeline.play();

        try {
            progressBar.setVisible(true);
            getWeatherByCityName(cityInput.getText());
            progressBar.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
            progressBar.setVisible(false);
        }
    }

    private void getWeatherByCityName(String city) throws IOException {
        String url = String.format("http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=%s", city, API_KEY);
        getWeather(url);
    }

    private void getWeather(String url) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                handleWeatherResponse(response);
            }
        }
    }

    private void handleWeatherResponse(CloseableHttpResponse response) throws IOException {
        if (response.getStatusLine().getStatusCode() != 200) {
            System.out.println("An error occurred: " + response.getStatusLine().getReasonPhrase());
            return;
        }

        String responseBody = EntityUtils.toString(response.getEntity());
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(responseBody);
            updateWeatherData(jsonObject);
        } catch (ParseException e) {
            System.out.println("An error occurred while parsing the API response. Please try again.");
            e.printStackTrace();
        }
    }

    private void updateWeatherData(JSONObject jsonObject) {
        JSONObject main = (JSONObject) jsonObject.get("main");
        JSONObject wind = (JSONObject) jsonObject.get("wind");
        JSONObject sys = (JSONObject) jsonObject.get("sys");
        JSONObject weather = (JSONObject) ((JSONArray) jsonObject.get("weather")).get(0);
        String iconId = (String) weather.get("icon");

        temperatureOutput.setText(formatTemperature(main.get("temp")));
        humidityOutput.setText(formatHumidity(main.get("humidity")));
        pressureOutput.setText(formatPressure(main.get("pressure")));
        windSpeedOutput.setText(formatWindSpeed(wind.get("speed")));            
        sunriseOutput.setText(convertUnixToReadable(Long.parseLong(sys.get("sunrise").toString())));
        sunsetOutput.setText(convertUnixToReadable(Long.parseLong(sys.get("sunset").toString())));
        weatherDescriptionOutput.setText(weather.get("description").toString());

        Image image = new Image("http://openweathermap.org/img/wn/" + iconId + ".png");
        weatherIcon.setImage(image);
    }

    private String convertUnixToReadable(long timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
        return formatter.format(Instant.ofEpochSecond(timestamp));
    }

    private String formatTemperature(Object temperature) {
        return String.format("%.1f °C", Float.parseFloat(temperature.toString()));
    }

    private String formatHumidity(Object humidity) {
        return String.format("%s %%", humidity.toString());
    }

    private String formatPressure(Object pressure) {
        return String.format("%s hPa", pressure.toString());
    }

    private String formatWindSpeed(Object windSpeed) {
        return String.format("%.1f m/s", Float.parseFloat(windSpeed.toString()));
    }
}
