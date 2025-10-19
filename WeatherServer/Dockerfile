FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS base
ARG APP_UID=0
USER $APP_UID
WORKDIR /app
EXPOSE 8080
EXPOSE 8081

FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
ARG BUILD_CONFIGURATION=Release
WORKDIR /src
COPY ["WeatherServer/WeatherServer.csproj", "WeatherServer/"]
RUN dotnet restore "WeatherServer/WeatherServer.csproj"
COPY . .
WORKDIR "/src/WeatherServer"
RUN dotnet build "WeatherServer.csproj" -c $BUILD_CONFIGURATION -o /app/build

FROM build AS publish
ARG BUILD_CONFIGURATION=Release
RUN dotnet publish "WeatherServer.csproj" -c $BUILD_CONFIGURATION -o /app/publish /p:UseAppHost=false

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "WeatherServer.dll"]
