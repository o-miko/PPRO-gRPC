namespace WeatherFrontend.Services
{
    public class GreeterService
    {
        private readonly Greeter.GreeterClient _client;

        public GreeterService(Greeter.GreeterClient client)
        {
            _client = client;
        }

        public async Task<string> GreetAsync(string name, CancellationToken ct = default)
        {
            var reply = await _client.SayHelloAsync(new HelloRequest { Name = name }, cancellationToken: ct);
            return reply.Message;
        }
    }
}
