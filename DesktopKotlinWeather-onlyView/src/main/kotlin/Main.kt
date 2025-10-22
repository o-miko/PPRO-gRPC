

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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.rememberWindowState

data class Forecast(
    val id: Int,
    val datetime: String,
    val apparentTemp: Double,
    val precipitation: Double,
    val humidity: Int,
    val windSpeed: Double
)

@Composable
@Preview
fun WeatherApp() {
    var town by remember { mutableStateOf("") }
    var currentTemperature by remember { mutableStateOf<String?>(null) }
    var forecasts by remember { mutableStateOf(listOf<Forecast>()) }

    val padding = 8.dp
    val scrollState = rememberScrollState()
    val dateFormatter = DateTimeFormatter.ofPattern("d. M. yyyy")

    // Defaultní hodnoty (dnešní datum)
    var startDate by remember { mutableStateOf(LocalDate.now().format(dateFormatter)) }
    var endDate by remember { mutableStateOf(LocalDate.now().format(dateFormatter)) }

    // Obrázek + výsledek rozpoznání
    var selectedImageFile by remember { mutableStateOf<File?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var imageResult by remember { mutableStateOf<String?>(null) }

    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.padding(padding * 2).fillMaxSize().verticalScroll(scrollState),
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
                Spacer(Modifier.height(padding / 2))
                // Tlačítko pro načtení aktuální teploty z gRPC serveru
                Button(onClick = {
                    // TODO: napojení na gRPC server
                    currentTemperature = "Aktuální teplota je: todo °C"
                }) {
                    Text("Zobrazit aktuální teplotu")
                }
                Spacer(Modifier.height(padding))


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


                Spacer(Modifier.height(padding * 2))


                // Datumy od po - vedle sebe
                Row(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // rozestup mezi inputy
                ) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Od datumu") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("Po datum") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(padding / 2))
                // Tlačítko pro zobrazení předpovědi po hodinách
                Button(onClick = {
                    // TODO: napojení na gRPC server
                    // prozatimní simulovaná data
                    forecasts = List(10) {
                        Forecast(
                            id = it + 1,
                            datetime = "2025-10-${21 + it} 12:00",
                            apparentTemp = 20 + it * 0.5,
                            precipitation = it * 0.2,
                            humidity = 50 + it,
                            windSpeed = 3.5 + it * 0.1
                        )
                    }
                }) {
                    Text("Zobrazit předpověď")
                }


                Spacer(Modifier.height(padding * 2))

                // Tabulka s výsledky předpovědi počasí
                if (forecasts.isNotEmpty()) {
                    Text("Předpověď počasí", style = MaterialTheme.typography.h6)
                    Spacer(Modifier.height(padding))

                    // Box kvůli overlay scrollbaru
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(400.dp) // nastavíš výšku tabulky, zbytek se scrolluje
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
                                    Text("ID", Modifier.weight(0.1f))
                                    Text("Datum a čas", Modifier.weight(0.25f))
                                    Text("Pocitová teplota", Modifier.weight(0.2f))
                                    Text("Srážky", Modifier.weight(0.15f))
                                    Text("Vlhkost", Modifier.weight(0.15f))
                                    Text("Vítr", Modifier.weight(0.15f))
                                }
                                Divider()
                            }

                            // Položky tabulky
                            items(forecasts) { f ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(f.id.toString(), Modifier.weight(0.1f))
                                    Text(f.datetime, Modifier.weight(0.25f))
                                    Text("${f.apparentTemp} °C", Modifier.weight(0.2f))
                                    Text("${f.precipitation} mm", Modifier.weight(0.15f))
                                    Text("${f.humidity} %", Modifier.weight(0.15f))
                                    Text("${f.windSpeed} m/s", Modifier.weight(0.15f))
                                }
                                Divider()
                            }
                        }

                        // Scrollbar pro tebulku
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 2.dp),
                            adapter = rememberScrollbarAdapter(listState)
                        )
                    }
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
                        // TODO: napojení na gRPC server
                        imageResult = "todo"
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
        WeatherApp()
    }
}
