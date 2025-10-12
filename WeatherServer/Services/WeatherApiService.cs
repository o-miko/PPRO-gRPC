using Grpc.Core;
using OpenMeteo;
using WeatherServer.Protos;

namespace WeatherServer.Services
{
    public class WeatherApiService: WeatherService.WeatherServiceBase
    {
        private readonly OpenMeteoClient _meteoClient;

        public WeatherApiService()
        {
            _meteoClient = new OpenMeteoClient();
        }
        public override Task<CurrentResponse> GetCurrent(CurrentRequest request, ServerCallContext context)
        {
            return base.GetCurrent(request, context);
        }

        public override Task<HourlyResponse> GetHourly(HourlyRequest request, ServerCallContext context)
        {
            return base.GetHourly(request, context);
        }

        public override Task<DailyResponse> GetDaily(DailyRequest request, ServerCallContext context)
        {
            return base.GetDaily(request, context);
        }
    }
}
