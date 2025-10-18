using Google.Protobuf;

namespace WeatherFrontend.Services;

public class WeatherServiceImpl
{
    private readonly WeatherFrontend.WeatherService.WeatherServiceClient _client;

    public WeatherServiceImpl(WeatherService.WeatherServiceClient client)
    {
        _client = client;
    }

    public async Task<ActualTemperatureResponse> GetActualTemperature(string town)
    {
        return await _client.GetActualTemperatureAsync(new ActualTemperatureRequest()
        {
            Location = new Location()
            {
                Town = town
            }
        });
    }

    public async Task<ForecastResponse> GetForecast(float logtitude, float latitude, string startDate, string endDate, int forecastDays)
    {
        return await _client.GetForecastAsync(new ForecastRequest()
        {
            Longitude = logtitude,
            Latitude = latitude,
            Timezone = "GMT",
            StartDate = startDate,
            EndDate = endDate,
            ForecastDays = forecastDays
        });
    }

    public async Task<RecognizeWeatherResponse> RecognizeImage(Stream stream)
    {
        return await _client.RecognizeWeatherAsync(new RecognizeWeatherRequest()
        {
            Image = await ByteString.FromStreamAsync(stream)
        });
    }
}