FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS base
ARG APP_UID=0
USER $APP_UID
WORKDIR /app
EXPOSE 8080
EXPOSE 8081

FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
ARG BUILD_CONFIGURATION=Release
WORKDIR /src
COPY ["WeatherFrontend/WeatherFrontend.csproj", "WeatherFrontend/"]
RUN dotnet restore "WeatherFrontend/WeatherFrontend.csproj"
COPY . .
WORKDIR "/src/WeatherFrontend"
RUN dotnet build "WeatherFrontend.csproj" -c $BUILD_CONFIGURATION -o /app/build

FROM build AS publish
ARG BUILD_CONFIGURATION=Release
RUN dotnet publish "WeatherFrontend.csproj" -c $BUILD_CONFIGURATION -o /app/publish /p:UseAppHost=false

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "WeatherFrontend.dll"]
