# WeatherApp

WeatherApp is a simple JavaFX-based application that fetches and displays real-time weather data for any city.

## Features
- Current temperature in Celsius
- Current humidity in percentage
- Current pressure in hPa
- Current wind speed in m/s
- Sunrise and sunset times
- Weather description
- Weather icon corresponding to the current weather description

## Dependencies
This project relies on:
- JavaFX for the UI
- Apache HttpClient for making API requests
- JSON.simple for parsing the JSON response
- OpenWeatherMap API for fetching the weather data

## Running the Application
You need to have a Java 11+ environment set up on your machine. To run the application, use the following command:

java -jar WeatherApp.jar


Please note that the OpenWeatherMap API requires an API key. You can obtain it for free [here](https://openweathermap.org/api). After obtaining the key, put it in a file named `config.properties` and place it in the classpath.

## Future Enhancements
- Add an option to choose the temperature unit (Celsius or Fahrenheit)
- Improve error handling and user feedback for invalid city names or network issues
- Improve the application's visuals and animations

## Contributing
Contributions to improve this project are welcomed. Please feel free to fork the project and submit a pull request with your changes.

## License
This project is licensed under the MIT License.
