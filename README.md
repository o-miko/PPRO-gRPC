# Demonstrace gRPC (PPRO)

Tato aplikace slouží jako demonstrace technologie gRPC pro předmět PPRO (Pokročilé programování).

Aplikace je rozdělena na dvě hlavní části:

1.  **Ukázková část (Server + Web Klient):** Plně funkční demo ukazující komunikaci mezi dvěma službami (backend a frontend) pomocí gRPC.
2.  **Procvičovací část (Desktop Klient):** Studenti si sami naprogramují vlastního desktopového klienta v Kotlin Compose, který se bude jevit jako gRPC klient a bude komunikovat s gRPC serverem z první části.

---

## 1. Ukázková část (Demo)
❗**Demo aplikaci lze nalézt na:** http://165.232.64.242/ ❗

Tato část se skládá ze dvou oddělených aplikací, které běží ve vlastních Docker kontejnerech:

* **Backend (gRPC Server):** Služba, která poskytuje data o počasí a AI analýzu obrázků.
* **Frontend (gRPC Klient):** Webová aplikace, která konzumuje data ze serveru.

### 💡 Proč toto řešení?

Díky rozdělení na dva separátní kontejnery (kontejner F pro frontend a kontejner B pro backend) získáváme plné výhody Dockeru:

* **Izolace:** Každá služba běží ve svém vlastním, izolovaném prostředí.
* **Přenositelnost:** Aplikace poběží konzistentně všude, kde je nainstalován Docker.
* **Škálovatelnost:** Toto je klíčové. Můžeme využít nástroje jako Kubernetes k řízení výkonu. Pokud by byl například backend přetížený, můžeme mu snadno přidělit více prostředků (zvýšit počet replik kontejneru B), aniž by to ovlivnilo frontend. Kdybychom měli monolit (vše v jednom kontejneru), museli bychom škálovat celou aplikaci najednou.

Jádrem problému tohoto přístupu je ale **komunikace** mezi těmito oddělenými službami. Zde přichází na řadu **gRPC** jako vysoce výkonný a efektivní způsob, jak mohou tyto mikroslužby mezi sebou komunikovat.

### 🌦️ Co demo umí?

Praktická ukázka demonstruje načítání dat o počasí. Webový klient (frontend) si vyžádá data pro konkrétní místo od backend serveru, který mu je pošle. Ukázka také zahrnuje AI funkci pro rozpoznávání počasí na obrázku, což demonstruje, že přes gRPC lze efektivně posílat i objemově velké binární data (soubory).

### 🚀 Jak spustit demo

1.  Otevřete projekt (root složku ukázkové části) v IntelliJ IDEA nebo Rider.
2.  Otevřete terminál (konzoli) přímo v IDE.
3.  Sestavte Docker obrazy příkazem:
    ```bash
    docker compose build
    ```
4.  Spusťte kontejnery příkazem:
    ```bash
    docker compose up
    ```
5.  Po spuštění bude webový klient dostupný v prohlížeči (adresu, např. `http://localhost:8080`, naleznete ve výpisu konzole).

---

## 2. Procvičovací část (Desktop Klient)

V této části je vaším úkolem naprogramovat novou desktopovou aplikaci v Kotlinu (pomocí **Compose for Desktop**). Tato aplikace se bude chovat jako gRPC klient.

Aplikace se bude připojovat na **backend gRPC server** (z Části 1), který již běží v Dockeru a vystavuje své služby na portu **`5214`**.

Alternativní gRPC server (pokud nefunguje Docker): **`165.232.64.242`**, port: **`8080`**

### Krok 1: Vytvoření projektu

Pro vytvoření projektu použijeme IntelliJ IDEA a její vestavěný generátor.

1.  Otevřete IntelliJ IDEA.
2.  Zvolte `File > New > Project` (Soubor > Nový > Projekt).
3.  V levém menu vyberte generátor **"Compose for Desktop"**.
4.  Nastavte si `Name` (např. `PPRO_gRPC_Klient`), `Location` (umístění), `Group` (např. `com.example`) a `Artifact`.
5.  Ujistěte se, že máte vybranou kompatibilní verzi JDK (např. 17).
6.  Klikněte na `Create` (Vytvořit).

Tímto postupem vám IntelliJ IDEA vygeneruje základní šablonu projektu se souborem `build.gradle.kts` a hlavním souborem `/src/main/kotlin/Main.kt`.

> **Tip:** Pro zobrazení živého náhledu vašeho UI (@Preview) přímo v IDE si nainstalujte plugin **Compose Multiplatform IDE Support** z Marketplace.

### Krok 2: Úprava souboru build.gradle.kts

1.  Soubor `build.gradle.kts` slouží ke správě závislostí a konfiguraci celého projektu, včetně toho, jak se projekt sestavuje. Nově vytvořený projekt již tento soubor obsahuje, ale musíme ho rozšířit o pluginy a závislosti nezbytné pro gRPC a Protobuf.
2.  Po zkopírování níže uvedeného kódu do vašeho `build.gradle.kts` souboru si všimněte, že IDE (IntelliJ IDEA) pravděpodobně zobrazí v pravém horním rohu ikonu pro **"Load Gradle Changes"** (Nahrát Gradle změny). Je **nutné** na tuto ikonu kliknout. Tímto krokem donutíte IDE, aby stáhlo všechny nové závislosti (knihovny pro gRPC), které jsme právě přidali.
3.  Po úspěšném stažení závislostí **doporučujeme projekt poprvé spustit** (nebo alespoň sestavit pomocí `Build > Build Project`). Tím se spustí `protobuf` plugin, který automaticky vygeneruje potřebné třídy z `.proto` souboru (`Weather` a `WeatherServiceGrpcKt`). Bez tohoto kroku by vám IDE hlásilo chyby, že třídy jako `WeatherServiceGrpcKt` neexistují.

Výsledný kód tohoto souboru by měl vypadat takto:

```kotlin
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import com.google.protobuf.gradle.*

plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.protobuf") version "0.9.4"
}

group = "cz.uhk.fim"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("[https://maven.pkg.jetbrains.space/public/p/compose/dev](https://maven.pkg.jetbrains.space/public/p/compose/dev)")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)

    // gRPC a Protobuf
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("io.grpc:grpc-netty-shaded:1.63.0")
    implementation("io.grpc:grpc-protobuf:1.63.0")
    implementation("io.grpc:grpc-stub:1.63.0")
    implementation("io.grpc:grpc-kotlin-stub:1.4.1")
    implementation("com.google.protobuf:protobuf-java:3.25.3")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.3"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.63.0"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.1:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Kotlin-weather-frontend"
            packageVersion = "1.0.0"
        }
    }
}
```

### Krok 3: Zkopírování `.proto` souboru

Aby mohl klient komunikovat se serverem, potřebuje znát "kontrakt" – definici služeb a zpráv komunikace gRPC. Ten je definován v `.proto` souboru.

1.  V nově vytvořeném projektu vytvořte adresářovou strukturu `/src/main/proto`.
2.  Najděte projekt z **Ukázkové části (Demo) - WeatherFrontend** a zkopírujte z něj soubor `/Protos/weather.proto`.
3.  Vložte tento soubor do své nové aplikace do složky `/src/main/proto/weather.proto`.

Alternativně si můžete soubor `weather.proto` vytvořit ručně a zkopírovat do něj následující kód:

```proto
syntax = "proto3";

option csharp_namespace = "WeatherFrontend";

package weather;

message HourlyUnits {
  string time = 1;
  string temperature_2m = 2;
  string relative_humidity_2m = 3;
  string precipitation = 4;
  string windspeed_10m = 5;
  string weathercode = 6;
}

message Location {
  string town = 1;
}

message ActualTemperatureRequest {
  Location location = 1;
}

message CurrentData {
  float temperature = 1;
  string time = 2;
}

message CurrentUnits {
  string temperature = 1;
  string time = 2;
}

message ActualTemperatureResponse {
  float latitude = 1;
  float longitude = 2;
  float generation_time_ms = 3;
  int32 utc_offset_seconds = 4;
  string timezone = 5;
  string timezone_abbreviation = 6;
  float elevation = 7;

  CurrentData current = 8;
  CurrentUnits current_units = 9;

  HourlyData hourly = 10;
  HourlyUnits hourly_units = 11;
}

message ForecastRequest {
  float latitude = 1;
  float longitude = 2;
  string timezone = 3;

  int32 forecast_hours = 4;
  int32 forecast_days = 5;

  string start_date = 6;
  string end_date = 7;
}

message HourlyData {
  repeated string time = 1;
  repeated float temperature_2m = 2;
  repeated float apparent_temperature = 3;
  repeated int32 relativehumidity_2m = 4;
  repeated float precipitation = 5;
  repeated float windspeed_10m = 6;
  repeated int32 weathercode = 7;
}


message ForecastResponse {
  double latitude = 1;
  double longitude = 2;
  double elevation = 3;
  HourlyData hourly = 4;
}

message RecognizeWeatherRequest {
  bytes image = 1;
}

message RecognizeWeatherResponse {
  string weather = 1;
  float confidence = 2;
}

service WeatherService {
  rpc GetActualTemperature (ActualTemperatureRequest) returns (ActualTemperatureResponse);
  rpc GetForecast (ForecastRequest) returns (ForecastResponse);
  rpc RecognizeWeather (RecognizeWeatherRequest) returns (RecognizeWeatherResponse);
}
```

### Krok 4: Vytvoření gRPC služby (Service Layer)

Pro čistší kód je dobré oddělit logiku volání gRPC do samostatné třídy (služby).

1.  Vytvořte nový balíček (package) `service` (ve složce `/src/main/kotlin`).
2.  Vytvořte v něm nový Kotlin soubor `WeatherServiceImpl.kt`.
3.  Vložte do něj následující kód, který definuje metody pro volání serveru:

```kotlin
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
```

### Krok 5: Úprava uživatelského rozhraní (UI)

Posledním krokem je úprava souboru `/src/main/kotlin/Main.kt`.

Zde musíte rozšířit vygenerovanou funkci `@Composable fun App()` tak, aby obsahovala:

* **Proměnné** (např. `currentTemperature`) pro uložení vstupního i výstupního textu.
* **Vstupní pole** (např. `TextField`) pro zadání textu (název města, souřadnice atd.).
* **Výstupní pole** (např. `Text`) pro zobrazení odpovědi ze serveru (aktuální teplota, předpověď...).
* **Tlačítka** (`Button`) pro zavolání jednotlivých metod z `WeatherServiceImpl` (zjištění teploty, předpověď, rozpoznání obrázku).

Vaším úkolem je propojit UI komponenty s logikou ve `WeatherServiceImpl`, abyste mohli odesílat požadavky na běžící gRPC server a zobrazovat přijaté výsledky. (Nezapomeňte si vytvořit gRPC kanál `ManagedChannel` směřující na `localhost:5214` a vytvořit instanci `WeatherServiceImpl`.)

Metoda `main()` slouží jako vstupní bod pro spuštění celé desktopové aplikace a k definování vlastností jejího hlavního okna, jako je název (title) nebo výchozí rozměry.
volitelně: Úprava funkce main() o title a defaultní rozměry okna aplikace

Výsledný kód třídy `Main.kt` vypadá následovně:
```kotlin
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.skia.Image
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.rememberWindowState
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.launch
import weather.Weather
import service.WeatherServiceImpl
import weather.WeatherServiceGrpcKt
import java.time.temporal.ChronoUnit


@Composable
@Preview
fun App() {
    // Stavy aplikace
    var town by remember { mutableStateOf("") }
    var currentTemperature by remember { mutableStateOf<String?>(null) }
    var responseCurrentTemperature by remember { mutableStateOf<Weather.ActualTemperatureResponse?>(null) }
    var forecasts by remember { mutableStateOf<Weather.ForecastResponse?>(null) }

    val scrollState = rememberScrollState()
    val inputDateFormatter = DateTimeFormatter.ofPattern("d. M. yyyy")
    var errorStr by remember { mutableStateOf<String?>(null) }

    // Defaultní hodnoty (dnešní datum)
    var startDateStr by remember { mutableStateOf(LocalDate.now().plusDays(1).format(inputDateFormatter)) }
    var endDateStr by remember { mutableStateOf(LocalDate.now().plusDays(1).format(inputDateFormatter)) }

    // Obrázek + výsledek rozpoznání
    var selectedImageFile by remember { mutableStateOf<File?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var imageResult by remember { mutableStateOf<String?>(null) }

    // Stavy pro připojení na gRPC server
    var ipAddress by remember { mutableStateOf("127.0.0.1") }
    var port by remember { mutableStateOf("5214") }
    var weatherService by remember { mutableStateOf<WeatherServiceImpl?>(null) }
    var connectionError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    MaterialTheme {
        // Sekce pro připojení gRPC
        if (weatherService == null) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Připojení k gRPC serveru", style = MaterialTheme.typography.h6)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        label = { Text("IP Adresa serveru") },
                        modifier = Modifier.weight(2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Port") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(onClick = {
                        coroutineScope.launch {
                            try {
                                val channel = ManagedChannelBuilder.forAddress(ipAddress, port.toInt())
                                    .usePlaintext()
                                    .build()
                                val stub = WeatherServiceGrpcKt.WeatherServiceCoroutineStub(channel)
                                weatherService = WeatherServiceImpl(stub) // Uložení služby do stavu
                                connectionError = null // Smazání chyby, pokud byla
                            } catch (e: Exception) {
                                e.printStackTrace()
                                connectionError = "Chyba připojení: ${e.message}"
                                weatherService = null
                            }
                        }
                    }) {
                        Text("Připojit")
                    }
                }
                // Zobrazení chyby připojení
                connectionError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colors.error)
                }
            }
        }
        // Hlavní obsah aplikace - zobrazí se po připojení
        else {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxSize().verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tlačítko pro změnu serveru
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { weatherService = null },
                            modifier = Modifier.align(Alignment.CenterStart) // Zarovnání doleva
                        ) {
                            Text("Změnit gRPC server")
                        }
                    }
                    Spacer(Modifier.height(8.dp)) // Mezera pod tlačítkem

                    // Název města
                    OutlinedTextField(
                        value = town,
                        onValueChange = { town = it },
                        label = { Text("Zadejte město") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                    Spacer(Modifier.height(4.dp))
                    // Tlačítko pro načtení aktuální teploty z gRPC serveru
                    Button(onClick = {
                        // Načtení aktuální teploty
                        coroutineScope.launch {
                            try {
                                // Vykřičníky (!!) říkají kompilátoru: "Víme, že weatherService zde není null."
                                // Jsme v 'else' bloku, který se spustí jen pokud 'weatherService' není null.
                                responseCurrentTemperature = weatherService!!.getActualTemperature(town)

                                currentTemperature =
                                    "Aktuální teplota: ${responseCurrentTemperature!!.current.temperature} ${responseCurrentTemperature!!.currentUnits.temperature}"
                            } catch (e: Exception) {
                                currentTemperature = "Město nenalezeno"
                            }

                        }

                    }) {
                        Text("Zobrazit aktuální teplotu")
                    }
                    Spacer(Modifier.height(8.dp))


                    // Výstupní text aktuální teplota
                    if (currentTemperature != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .padding(top = 8.dp),
                            elevation = 4.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentTemperature!!,
                                    style = MaterialTheme.typography.h6
                                )
                            }
                        }
                    }


                    Spacer(Modifier.height(16.dp))


                    // Datumy od po - vedle sebe
                    Row(
                        modifier = Modifier.fillMaxWidth(0.7f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp) // rozestup mezi inputy
                    ) {
                        OutlinedTextField(
                            value = startDateStr,
                            onValueChange = { startDateStr = it },
                            label = { Text("Od datumu") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endDateStr,
                            onValueChange = { endDateStr = it },
                            label = { Text("Po datum") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    // Tlačítko pro zobrazení předpovědi po hodinách
                    Button(onClick = {
                        coroutineScope.launch {
                            try {
                                if (responseCurrentTemperature == null) {
                                    error("Nebylo vybráno žádné město.")
                                }
                                val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                                // převod na LocalDate
                                val startDateParsed = LocalDate.parse(startDateStr!!, inputDateFormatter)
                                val endDateParsed = LocalDate.parse(endDateStr!!, inputDateFormatter)

                                // rozdíl v počtu dní
                                val days = ChronoUnit.DAYS.between(startDateParsed, endDateParsed).toInt()
                                if (days < 0) {
                                    error("Datum po je dříve než datum od.")
                                }

                                // převod na formát yyyy-MM-dd
                                val startDateFormatted = startDateParsed.format(outputFormatter)
                                val endDateFormatted = endDateParsed.format(outputFormatter)

                                forecasts = weatherService!!.getForecast(
                                    responseCurrentTemperature?.latitude ?: 0f,
                                    responseCurrentTemperature?.longitude ?: 0f,
                                    startDateFormatted,
                                    endDateFormatted,
                                    days,
                                    24
                                )

                                errorStr = null  // reset chyby
                            } catch (e: Exception) {
                                println("Chyba při načítání předpovědi: ${e.message}")
                                errorStr = "Chyba při načítání předpovědi: ${e.message}"
                                forecasts = null
                            }
                        }
                    }) {
                        Text("Zobrazit předpověď")
                    }

                    // Text erroru
                    if (errorStr != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .padding(top = 4.dp),
                            elevation = 4.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = errorStr!!, style = MaterialTheme.typography.body2
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))


                    // Tabulka s výsledky předpovědi počasí
                    val hourly = forecasts?.hourly
                    if (forecasts?.isInitialized ?: false && hourly != null) {
                        Text("Předpověď počasí", style = MaterialTheme.typography.h6)
                        Spacer(Modifier.height(8.dp))

                        // Box kvůli overlay scrollbaru
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(350.dp) // výška tabulky, zbytek se scrolluje
                        ) {
                            val listState = rememberLazyListState()

                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Hlavička tabulky
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("#", Modifier.weight(0.1f))
                                        Text("Datum a čas", Modifier.weight(0.25f))
                                        Text("Pocitová teplota", Modifier.weight(0.2f))
                                        Text("Srážky", Modifier.weight(0.15f))
                                        Text("Relativní vlhkost", Modifier.weight(0.15f))
                                        Text("Rychlost větru", Modifier.weight(0.15f))
                                    }
                                    Divider()
                                }

                                // Položky tabulky — iterujeme podle délky timeList
                                items(hourly.timeList.size) { index ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text((index + 1).toString(), Modifier.weight(0.1f))
                                        Text(hourly.timeList[index], Modifier.weight(0.25f))
                                        Text(
                                            "${hourly.apparentTemperatureList.getOrNull(index) ?: "-"} °C",
                                            Modifier.weight(0.2f)
                                        )
                                        Text(
                                            "${hourly.precipitationList.getOrNull(index) ?: "-"} mm",
                                            Modifier.weight(0.15f)
                                        )
                                        Text(
                                            "${hourly.relativehumidity2MList.getOrNull(index) ?: "-"} %",
                                            Modifier.weight(0.15f)
                                        )
                                        Text(
                                            "${hourly.windspeed10MList.getOrNull(index) ?: "-"} m/s",
                                            Modifier.weight(0.15f)
                                        )
                                    }
                                    Divider()
                                }
                            }

                            // Scrollbar pro tabulku
                            VerticalScrollbar(
                                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 2.dp),
                                adapter = rememberScrollbarAdapter(listState)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }


                    // Výběr obrázku k rozpoznání
                    Text("Vyberte obrázek k rozpoznání počasí", style = MaterialTheme.typography.body2)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = {
                            val dialog = FileDialog(null as Frame?, "Vyberte obrázek", FileDialog.LOAD)
                            dialog.isVisible = true
                            if (dialog.file != null) {
                                val file = File(dialog.directory, dialog.file)
                                selectedImageFile = file

                                // Načtení obrázku pomocí Skia Image (funkční v Compose Desktop)
                                try {
                                    val bytes = file.readBytes()
                                    val skiaImage = Image.makeFromEncoded(bytes)
                                    selectedImageBitmap = skiaImage.toComposeImageBitmap()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    selectedImageBitmap = null
                                }
                            }
                        }) {
                            Text("📁 Vybrat obrázek")
                        }

                        Spacer(Modifier.width(16.dp))

                        // Výpis názvu vybraného souboru
                        selectedImageFile?.let {
                            Text(it.name)
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Náhled obrázku 300x200 px
                    selectedImageBitmap?.let { bitmap ->
                        Box(
                            modifier = Modifier
                                .size(width = 300.dp, height = 200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colors.onSurface.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Náhled vybraného obrázku",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Tlačítko Rozpoznat obrázek
                    selectedImageFile?.let { file ->
                        Spacer(Modifier.height(4.dp))

                        // Tlačítko Rozpoznat obrázek
                        Button(onClick = {
                            coroutineScope.launch {
                                try {
                                    val imageBytes = file.readBytes() // načtení obsahu obrázku
                                    val response = weatherService!!.recognizeImage(imageBytes) // volání gRPC

                                    // Aktualizace Compose UI přímo
                                    imageResult = "${response.weather} - ${response.confidence * 100}%"
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    imageResult = "Chyba při rozpoznání obrázku: ${e.message}"
                                }
                            }
                        }) {
                            Text("Rozpoznat obrázek")
                        }
                    }


                    // Výstupní text
                    if (imageResult != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .padding(top = 8.dp),
                            elevation = 4.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = imageResult!!,
                                    style = MaterialTheme.typography.h6
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                } // Column

                // Hlavní scrollbar připojený na scrollState
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(scrollState),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                )
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Weather App",
        state = rememberWindowState(
            width = 1200.dp,
            height = 900.dp
        )
    ) {
        App()
    }
}

```

## Příklad z praxe
https://github.com/FgForrest/evitalab
