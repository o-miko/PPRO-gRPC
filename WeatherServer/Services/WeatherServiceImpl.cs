using Grpc.Core;
using MapsterMapper;
using OpenMeteo;
using Weather;

namespace WeatherServer.Services;

public class WeatherServiceImpl : WeatherService.WeatherServiceBase
{
    private readonly OpenMeteoClient _meteoClient = new OpenMeteoClient();
    private readonly IMapper _mapper;

    public WeatherServiceImpl(IMapper mapper)
    {
        _mapper = mapper;
    }

    public override async Task<ActualTemperatureResponse> GetActualTemperature(ActualTemperatureRequest request, ServerCallContext context)
    {
        WeatherForecast? forecast = await _meteoClient.QueryAsync(request.Location.Town);
        if (forecast == null)
        {
            throw new RpcException(new Status(StatusCode.NotFound, $"No forecast found for town '{request.Location.Town}'"));
        }

        return _mapper.Map<ActualTemperatureResponse>(forecast);
    }

    public override async Task<ForecastResponse> GetForecast(ForecastRequest request, ServerCallContext context)
    {
        var options = new WeatherForecastOptions
        {
            Latitude = request.Latitude,
            Longitude = request.Longitude,
            Timezone = request.Timezone,
            Start_date = request.StartDate,
            End_date = request.EndDate,
            Timeformat = TimeformatType.iso8601,
            Temperature_Unit = TemperatureUnitType.celsius,
            Windspeed_Unit = WindspeedUnitType.kmh,
            Precipitation_Unit = PrecipitationUnitType.mm,
            Cell_Selection = CellSelectionType.land,

            Hourly = new HourlyOptions
        {
                HourlyOptionsParameter.temperature_2m,        // teplota
                HourlyOptionsParameter.apparent_temperature,  // pocitová teplota
                HourlyOptionsParameter.relativehumidity_2m,   // relativní vlhkost
                HourlyOptionsParameter.precipitation,         // srážky
                HourlyOptionsParameter.windspeed_10m,         // vítr
                HourlyOptionsParameter.weathercode            // (doporučuji přidat)
            
        }
        };

        var forecast = await _meteoClient.QueryAsync(options);

        if (forecast?.Hourly == null)
            throw new RpcException(new Status(StatusCode.NotFound, "No forecast data available"));

        try
        {
            return new ForecastResponse
            {
                Longitude = forecast.Longitude,
                Latitude  = forecast.Latitude,
                Elevation = forecast.Elevation,
                Hourly = new HourlyData
                {
                    Time                 = { forecast.Hourly.Time ?? Array.Empty<string>() },
                    Temperature2M        = { Clean(forecast.Hourly.Temperature_2m) },
                    ApparentTemperature  = { Clean(forecast.Hourly.Apparent_temperature) },
                    Relativehumidity2M   = { Clean(forecast.Hourly.Relativehumidity_2m) },
                    Precipitation        = { Clean(forecast.Hourly.Precipitation) },
                    Windspeed10M         = { Clean(forecast.Hourly.Windspeed_10m) },
                    Weathercode          = { Clean(forecast.Hourly.Weathercode) }
                }
            };
        }
        catch (Exception ex)
        {
            throw new RpcException(new Status(StatusCode.Internal, ex.Message));
        }

    }
    private IEnumerable<float> Clean(float?[]? a) =>
        a?.Where(v => v.HasValue).Select(v => v!.Value) ?? Enumerable.Empty<float>();

    private IEnumerable<int> Clean(int?[]? a) =>
        a?.Where(v => v.HasValue).Select(v => v!.Value) ?? Enumerable.Empty<int>();

    public override Task<RecognizeWeatherResponse> RecognizeWeather(RecognizeWeatherRequest request, ServerCallContext context)
    {
        MLWeather.ModelOutput modelOutput = MLWeather.Predict(new MLWeather.ModelInput()
        {
            ImageSource = request.Image.ToByteArray()
        });
        
        RecognizeWeatherResponse response = new RecognizeWeatherResponse() { Weather = modelOutput.PredictedLabel, Confidence = modelOutput.Score.Max() };
        return Task.FromResult(response);
    }
}