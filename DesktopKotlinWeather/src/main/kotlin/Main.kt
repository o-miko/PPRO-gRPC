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
import service.WeatherServiceImpl
import weather.Weather
import weather.WeatherServiceGrpcKt
import java.time.temporal.ChronoUnit


@Composable
@Preview
fun App() {
    var town by remember { mutableStateOf("") }
    var currentTemperature by remember { mutableStateOf<String?>(null) }
    var responseCurrentTemperature by remember { mutableStateOf<Weather.ActualTemperatureResponse?>(null) }
    var forecasts by remember { mutableStateOf<Weather.ForecastResponse?>(null) }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val inputDateFormatter = DateTimeFormatter.ofPattern("d. M. yyyy")
    var errorStr by remember { mutableStateOf<String?>(null) }

    // Defaultní hodnoty (dnešní datum)
    var startDateStr by remember { mutableStateOf(LocalDate.now().plusDays(1).format(inputDateFormatter)) }
    var endDateStr by remember { mutableStateOf(LocalDate.now().plusDays(1).format(inputDateFormatter)) }

    // Obrázek + výsledek rozpoznání
    var selectedImageFile by remember { mutableStateOf<File?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var imageResult by remember { mutableStateOf<String?>(null) }

    val channel = ManagedChannelBuilder.forAddress("127.0.0.1", 5214).usePlaintext().build()
    val stub = WeatherServiceGrpcKt.WeatherServiceCoroutineStub(channel)
    val weatherService = WeatherServiceImpl(stub)

    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize().verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                            responseCurrentTemperature = weatherService.getActualTemperature(town)

                            currentTemperature = "Aktuální teplota: ${responseCurrentTemperature!!.current.temperature} ${responseCurrentTemperature!!.currentUnits.temperature}"
                        }catch (e: Exception) {
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
                            if(responseCurrentTemperature == null){
                                error("Nebylo vybráno žádné město.")
                            }
                            val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                            // převod na LocalDate
                            val startDateParsed = LocalDate.parse(startDateStr!!, inputDateFormatter)
                            val endDateParsed = LocalDate.parse(endDateStr!!, inputDateFormatter)

                            // rozdíl v počtu dní
                            val days = ChronoUnit.DAYS.between(startDateParsed, endDateParsed).toInt()
                            if(days < 0){
                                error("Datum po je dříve než datum od.")
                            }

                            // převod na formát yyyy-MM-dd
                            val startDateFormatted = startDateParsed.format(outputFormatter)
                            val endDateFormatted = endDateParsed.format(outputFormatter)

                            forecasts = weatherService.getForecast(
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


                // Zpráva erroru
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
                                    Text("${hourly.apparentTemperatureList.getOrNull(index) ?: "-"} °C", Modifier.weight(0.2f))
                                    Text("${hourly.precipitationList.getOrNull(index) ?: "-"} mm", Modifier.weight(0.15f))
                                    Text("${hourly.relativehumidity2MList.getOrNull(index) ?: "-"} %", Modifier.weight(0.15f))
                                    Text("${hourly.windspeed10MList.getOrNull(index) ?: "-"} m/s", Modifier.weight(0.15f))
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
                                val response = weatherService.recognizeImage(imageBytes) // volání gRPC

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
