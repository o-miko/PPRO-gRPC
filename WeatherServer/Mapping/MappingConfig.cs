using Mapster;
using OpenMeteo;
using Weather;

public static class MappingConfig
{
    public static void RegisterMappings()
    {
        TypeAdapterConfig.GlobalSettings.Default
            .NameMatchingStrategy(NameMatchingStrategy.Flexible);

        TypeAdapterConfig<WeatherForecast, ForecastResponse>
            .NewConfig()
            .Map(dest => dest.Latitude,  src => src.Latitude)
            .Map(dest => dest.Longitude, src => src.Longitude)
            .Map(dest => dest.Elevation, src => src.Elevation)
            .Map(dest => dest.Hourly, src => src.Hourly.Adapt<HourlyData>());

        TypeAdapterConfig<Hourly, HourlyData>
            .NewConfig()
            .MapWith(src => new HourlyData
            {
                Time = { src.Time ?? Array.Empty<string>() },

                Temperature2M        = { Clean(src.Temperature_2m) },
                ApparentTemperature  = { Clean(src.Apparent_temperature) },
                Relativehumidity2M   = { Clean(src.Relativehumidity_2m) },
                Precipitation        = { Clean(src.Precipitation) },
                Windspeed10M         = { Clean(src.Windspeed_10m) },
                Weathercode          = { Clean(src.Weathercode) }
            });
    }
    private static IEnumerable<float> Clean(float?[]? a)
        => a?.Where(x => x.HasValue).Select(x => x.Value) ?? Enumerable.Empty<float>();

    private static IEnumerable<int> Clean(int?[]? a)
        => a?.Where(x => x.HasValue).Select(x => x.Value) ?? Enumerable.Empty<int>();
}