using WeatherFrontend;
using WeatherFrontend.Components;
using WeatherFrontend.Services;
AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddRazorComponents()
    .AddInteractiveServerComponents();

var grpcUrl = Environment.GetEnvironmentVariable("GRPC_SERVER_URL")
             ?? builder.Configuration["Grpc:WeatherUrl"]
             ?? "http://127.0.0.1:5214";

builder.Services.AddGrpcClient<Greeter.GreeterClient>(o =>
{
    o.Address = new Uri(grpcUrl);
});
builder.Services.AddGrpcClient<WeatherService.WeatherServiceClient>(o =>
{
    o.Address = new Uri(grpcUrl);
});
builder.Services.AddScoped<GreeterService>();
builder.Services.AddScoped<WeatherServiceImpl>();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Error", createScopeForErrors: true);
    // The default HSTS value is 30 days. You may want to change this for production scenarios, see https://aka.ms/aspnetcore-hsts.
    app.UseHsts();
}

app.UseHttpsRedirection();

app.UseStaticFiles();
app.UseAntiforgery();

app.MapRazorComponents<App>()
    .AddInteractiveServerRenderMode();

app.Run();
