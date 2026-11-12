package sv.edu.utec.etps1.registroincidencias

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.lifecycle.ViewModel
import sv.edu.utec.etps1.registroincidencias.ui.theme.RegistroIncidenciasTheme
import kotlin.math.sqrt

// ============================================================================
// SEMANA 16: Contenedor de estado (ViewModel) separado de la UI
// Guarda los datos del formulario en memoria y sobrevive a rotaciones de pantalla.
// ============================================================================
class RegistroIncidenciasViewModel : ViewModel() {

    // Estados del formulario administrados fuera del composable.
    // 'private set' permite que la UI lea cada valor, pero obliga a modificarlos
    // únicamente mediante las funciones de evento definidas abajo.
    var concepto by mutableStateOf("")
        private set

    var monto by mutableStateOf("")
        private set

    var categoria by mutableStateOf("Comida")
        private set

    var metodoPago by mutableStateOf("Efectivo")
        private set

    var mensaje by mutableStateOf("Aún no hay gastos registrados")
        private set

    var esError by mutableStateOf(false)
        private set

    var totalRegistros by mutableStateOf(0)
        private set

    // Evento de escritura para el campo Concepto (conectado a onValueChange)
    fun actualizarConcepto(valor: String) {
        concepto = valor
        if (esError) esError = false
    }

    // Evento de escritura para el campo Monto (conectado a onValueChange)
    fun actualizarMonto(valor: String) {
        monto = valor
        if (esError) esError = false
    }

    // Evento de selección para los chips de Categoría (conectado a onClick)
    fun seleccionarCategoria(nuevaCategoria: String) {
        categoria = nuevaCategoria
    }

    // Evento de selección para las tarjetas de Método de pago (conectado a clickable)
    fun seleccionarMetodoPago(nuevoMetodo: String) {
        metodoPago = nuevoMetodo
    }

    // Evento de validación y registro: recibe el estado del sensor como texto simple
    // para que el ViewModel no dependa del contexto ni del hardware del dispositivo.
    fun registrarGasto(estadoMovimiento: String) {
        if (concepto.isBlank() || monto.isBlank()) {
            mensaje = "Advertencia: Ingresa tanto el concepto como el monto gastado."
            esError = true
            return
        }

        totalRegistros++
        mensaje = "Gasto #$totalRegistros registrado con éxito:\n" +
                "• Concepto: $concepto\n" +
                "• Monto: $$monto\n" +
                "• Categoría: $categoria\n" +
                "• Pago: $metodoPago\n" +
                "• Diagnóstico: $estadoMovimiento"
        esError = false
    }

    // Evento de reinicio: devuelve los campos y selecciones a su estado inicial
    fun limpiarFormulario() {
        concepto = ""
        monto = ""
        categoria = "Comida"
        metodoPago = "Efectivo"
        mensaje = "Aún no hay gastos registrados"
        esError = false
    }
}

// Instrumentación oficial de la Semana 14 (permite verificar onDestroy -> onCreate en Logcat)
class MainActivity : ComponentActivity() {
    private val tag = "TPIS1_LIFECYCLE"

    // by viewModels() vincula el ViewModel a la Activity y conserva sus datos al rotar
    private val vm: RegistroIncidenciasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate")
        setContent {
            RegistroIncidenciasTheme {
                RegistroIncidenciasApp(vm = vm)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(tag, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(tag, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(tag, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy")
    }
}

@Preview(showBackground = true)
@Composable
fun RegistroIncidenciasPreview() {
    RegistroIncidenciasTheme {
        RegistroIncidenciasApp(vm = RegistroIncidenciasViewModel())
    }
}

// El composable recibe el ViewModel como parámetro para leer estado y enviar eventos
@Composable
fun RegistroIncidenciasApp(vm: RegistroIncidenciasViewModel) {
    // Controladores propios de la vista (pertenecen a la UI, nunca al ViewModel)
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Estados ligados al hardware del dispositivo en tiempo real (pertenecen a la UI)
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
                // Limita el redibujado para no saturar el emulador ni bloquear la escritura
                private var ultimaActualizacionMs = 0L

                override fun onSensorChanged(event: SensorEvent) {
                    val ahora = System.currentTimeMillis()
                    if (ahora - ultimaActualizacionMs < 350L) return
                    ultimaActualizacionMs = ahora

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

    // Estado efímero de desplazamiento visual (pertenece únicamente a la UI)
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
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
            text = "Semana 16: Estado de pantalla con ViewModel.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Consume el conteo guardado en el ViewModel
        AssistChip(
            onClick = { },
            label = { Text("Gastos anotados en sesión: ${vm.totalRegistros}") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Bloque de diagnóstico del dispositivo (estado instantáneo de la UI)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Diagnóstico del dispositivo",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sensor: Acelerómetro (TYPE_ACCELEROMETER)",
                    style = MaterialTheme.typography.bodySmall,
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

        // Campo 1: Lee vm.concepto y envía eventos de texto con vm::actualizarConcepto
        OutlinedTextField(
            value = vm.concepto,
            onValueChange = vm::actualizarConcepto,
            label = { Text("Concepto del gasto (ej. Almuerzo)") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        keyboardController?.show()
                    }
                },
            isError = vm.esError && vm.concepto.isBlank(),
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

        // Campo 2: Lee vm.monto y envía eventos de texto con vm::actualizarMonto
        OutlinedTextField(
            value = vm.monto,
            onValueChange = vm::actualizarMonto,
            label = { Text("Monto gastado ($)") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        keyboardController?.show()
                    }
                },
            isError = vm.esError && vm.monto.isBlank(),
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

        // Chips de categoría: leen vm.categoria y envían la selección al ViewModel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Comida", "Transporte", "Servicios", "Ocio").forEach { item ->
                FilterChip(
                    selected = vm.categoria == item,
                    onClick = { vm.seleccionarCategoria(item) },
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

        // Tarjetas de pago: leen vm.metodoPago y notifican el toque al ViewModel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("Efectivo", "Tarjeta").forEach { metodo ->
                val seleccionado = vm.metodoPago == metodo
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { vm.seleccionarMetodoPago(metodo) },
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

        // Botones de acción: cierran el teclado en la UI y delegan la lógica al ViewModel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    vm.registrarGasto(estadoMovimiento)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Registrar Gasto")
            }

            OutlinedButton(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    vm.limpiarFormulario()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Limpiar")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tarjeta de resultado o advertencia controlada desde el ViewModel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (vm.esError) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Text(
                text = vm.mensaje,
                modifier = Modifier.padding(14.dp),
                color = if (vm.esError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
