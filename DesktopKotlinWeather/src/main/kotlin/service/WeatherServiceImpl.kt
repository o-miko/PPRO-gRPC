package service

import com.google.protobuf.ByteString
import weather.Weather
import weather.WeatherServiceGrpcKt


class WeatherServiceImpl(private val stub: WeatherServiceGrpcKt.WeatherServiceCoroutineStub) {

    suspend fun getActualTemperature(town: String): Weather.ActualTemperatureResponse {
        val request = Weather.ActualTemperatureRequest.newBuilder()
            .setLocation(Weather.Location.newBuilder().setTown(town))
            .build()
        return stub.getActualTemperature(request)
    }

    suspend fun getForecast(
        latitude: Float,
        longitude: Float,
        startDate: String,
        endDate: String,
        forecastDays: Int,
        forecastHours: Int
    ): Weather.ForecastResponse {
        val request = Weather.ForecastRequest.newBuilder()
            .setLatitude(latitude)
            .setLongitude(longitude)
            .setTimezone("GMT")
            .setStartDate(startDate)
            .setEndDate(endDate)
            .setForecastDays(forecastDays)
            .setForecastHours(forecastHours)
            .build()
        return stub.getForecast(request)
    }

    suspend fun recognizeImage(imageBytes: ByteArray): Weather.RecognizeWeatherResponse {
        val request = Weather.RecognizeWeatherRequest.newBuilder()
            .setImage(ByteString.copyFrom(imageBytes))
            .build()
        return stub.recognizeWeather(request)
    }
}