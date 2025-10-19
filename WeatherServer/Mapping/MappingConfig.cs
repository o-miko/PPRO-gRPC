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
        
        TypeAdapterConfig<OpenMeteo.WeatherForecast, Weather.ActualTemperatureResponse>
    .NewConfig()
    .Map(d => d.Latitude, s => s.Latitude)
    .Map(d => d.Longitude, s => s.Longitude)
    .Map(d => d.Elevation, s => s.Elevation)
    .Map(d => d.GenerationTimeMs, s => s.GenerationTime)
    .Map(d => d.UtcOffsetSeconds, s => s.UtcOffset)
    .Map(d => d.Timezone, s => s.Timezone ?? "")
    .Map(d => d.TimezoneAbbreviation, s => s.TimezoneAbbreviation ?? "")
    .Map(d => d.Current, s => s.Current == null ? null : s.Current.Adapt<Weather.CurrentData>())
    .Map(d => d.CurrentUnits, s => s.CurrentUnits == null ? null : s.CurrentUnits.Adapt<Weather.CurrentUnits>())
    .Map(d => d.HourlyUnits, s => s.HourlyUnits == null ? null : s.HourlyUnits.Adapt<Weather.HourlyUnits>())
    .Ignore(d => d.Hourly)
    .AfterMapping((s, d) =>
    {
        d.Hourly ??= new Weather.HourlyData();
        if (s.Hourly == null) return;

        if (s.Hourly.Time != null) d.Hourly.Time.AddRange(s.Hourly.Time);
        if (s.Hourly.Temperature_2m != null) d.Hourly.Temperature2M.AddRange(s.Hourly.Temperature_2m.Select(v => (float)v));
        if (s.Hourly.Apparent_temperature != null) d.Hourly.ApparentTemperature.AddRange(s.Hourly.Apparent_temperature.Select(v => (float)v));
        if (s.Hourly.Relativehumidity_2m != null) d.Hourly.Relativehumidity2M.AddRange(s.Hourly.Relativehumidity_2m.Select(v => (int)v));
        if (s.Hourly.Precipitation != null) d.Hourly.Precipitation.AddRange(s.Hourly.Precipitation.Select(v => (float)v));
        if (s.Hourly.Windspeed_10m != null) d.Hourly.Windspeed10M.AddRange(s.Hourly.Windspeed_10m.Select(v => (float)v));
        if (s.Hourly.Weathercode != null) d.Hourly.Weathercode.AddRange(s.Hourly.Weathercode.Select(v => (int)v));
    });

TypeAdapterConfig<OpenMeteo.Current, Weather.CurrentData>
    .NewConfig()
    .Map(d => d.Temperature, s => s.Temperature)
    .Map(d => d.Time, s => s.Time ?? "");

TypeAdapterConfig<OpenMeteo.CurrentUnits, Weather.CurrentUnits>
    .NewConfig()
    .Map(d => d.Temperature, s => s.Temperature ?? "")
    .Map(d => d.Time, s => s.Time ?? "");

TypeAdapterConfig<OpenMeteo.HourlyUnits, Weather.HourlyUnits>
    .NewConfig()
    .Map(d => d.Time, s => s.Time ?? "")
    .Map(d => d.Temperature2M, s => s.Temperature_2m ?? "")
    .Map(d => d.RelativeHumidity2M, s => s.Relativehumidity_2m ?? "")
    .Map(d => d.Precipitation, s => s.Precipitation ?? "")
    .Map(d => d.Windspeed10M, s => s.Windspeed_10m ?? "")
    .Map(d => d.Weathercode, s => s.Weathercode ?? "");
    }
    private static IEnumerable<float> Clean(float?[]? a)
        => a?.Where(x => x.HasValue).Select(x => x.Value) ?? Enumerable.Empty<float>();

    private static IEnumerable<int> Clean(int?[]? a)
        => a?.Where(x => x.HasValue).Select(x => x.Value) ?? Enumerable.Empty<int>();
}