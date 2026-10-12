package sv.edu.utec.etps1.registroincidencias

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sv.edu.utec.etps1.registroincidencias.ui.theme.RegistroIncidenciasTheme
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegistroIncidenciasTheme {
                RegistroIncidenciasApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistroIncidenciasPreview() {
    RegistroIncidenciasTheme {
        RegistroIncidenciasApp()
    }
}

@Composable
fun RegistroIncidenciasApp() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Estados para formulario
    var concepto by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Comida") }
    var metodoPago by remember { mutableStateOf("Efectivo") }
    var mensaje by remember { mutableStateOf("Aún no hay gastos registrados") }
    var esError by remember { mutableStateOf(false) }
    var totalRegistros by remember { mutableStateOf(0) }

    // Estados y administración del sensor acelerómetro (Semana 11)
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    var sensorDisponible by remember { mutableStateOf(accelerometer != null) }
    var ejeX by remember { mutableStateOf(0f) }
    var ejeY by remember { mutableStateOf(0f) }
    var ejeZ by remember { mutableStateOf(0f) }
    var estadoMovimiento by remember { mutableStateOf("Dispositivo en reposo") }

    // Ciclo de vida controlado: vinculación y liberación del sensor en Compose
    DisposableEffect(accelerometer) {
        if (accelerometer == null) {
            sensorDisponible = false
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    ejeX = x
                    ejeY = y
                    ejeZ = z

                    // Magnitud vectorial euclidiana para determinar estabilidad
                    val magnitud = sqrt((x * x + y * y + z * z).toDouble())
                    estadoMovimiento = when {
                        magnitud > 13.5 -> "Movimiento brusco o sacudida"
                        magnitud > 10.5 -> "En movimiento moderado"
                        else -> "Dispositivo en reposo estable"
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Control de Gastos",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Semana 11: Integración con sensor acelerómetro.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        AssistChip(
            onClick = { },
            label = { Text("Gastos anotados en sesión: $totalRegistros") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Bloque de monitoreo de sensor en tiempo real (Semana 11)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Sensor: Acelerómetro (TYPE_ACCELEROMETER)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (sensorDisponible) {
                    Text(
                        text = "Valores: X: ${"%.2f".format(ejeX)} | Y: ${"%.2f".format(ejeY)} | Z: ${"%.2f".format(ejeZ)} m/s²",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Estado: $estadoMovimiento",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "Acelerómetro físico no disponible en este entorno.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Campo 1: Fuerza apertura del teclado al ganar foco con ImeAction.Next
        OutlinedTextField(
            value = concepto,
            onValueChange = {
                concepto = it
                if (esError) esError = false
            },
            label = { Text("Concepto del gasto (ej. Almuerzo)") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        keyboardController?.show()
                    }
                },
            isError = esError && concepto.isBlank(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Campo 2: Fuerza apertura del teclado numérico al ganar foco con ImeAction.Done
        OutlinedTextField(
            value = monto,
            onValueChange = {
                monto = it
                if (esError) esError = false
            },
            label = { Text("Monto gastado ($)") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        keyboardController?.show()
                    }
                },
            isError = esError && monto.isBlank(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Categoría:",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.Start)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Comida", "Transporte", "Servicios", "Ocio").forEach { item ->
                FilterChip(
                    selected = categoria == item,
                    onClick = { categoria = item },
                    label = { Text(item) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Método de pago (toca para seleccionar):",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("Efectivo", "Tarjeta").forEach { metodo ->
                val seleccionado = metodoPago == metodo
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { metodoPago = metodo },
                    colors = CardDefaults.cardColors(
                        containerColor = if (seleccionado) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    border = if (seleccionado) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = metodo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (seleccionado) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    if (concepto.isBlank() || monto.isBlank()) {
                        mensaje = "Advertencia: Ingresa tanto el concepto como el monto gastado."
                        esError = true
                    } else {
                        totalRegistros++
                        mensaje = "Gasto #$totalRegistros registrado con éxito:\n" +
                                "• Concepto: $concepto\n" +
                                "• Monto: $$monto\n" +
                                "• Categoría: $categoria\n" +
                                "• Pago: $metodoPago\n" +
                                "• Estado dinámico: $estadoMovimiento"
                        esError = false
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Registrar Gasto")
            }

            OutlinedButton(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    concepto = ""
                    monto = ""
                    categoria = "Comida"
                    metodoPago = "Efectivo"
                    mensaje = "Aún no hay gastos registrados"
                    esError = false
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Limpiar")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (esError) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Text(
                text = mensaje,
                modifier = Modifier.padding(14.dp),
                color = if (esError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
