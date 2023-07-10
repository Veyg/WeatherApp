module com.weatherapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;

    opens com.weatherapp to javafx.fxml;
}
