package sv.edu.utec.etps1.registroincidencias

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sv.edu.utec.etps1.registroincidencias.ui.theme.RegistroIncidenciasTheme

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
    var concepto by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Comida") }
    var mensaje by remember { mutableStateOf("Aún no hay gastos registrados") }
    var esError by remember { mutableStateOf(false) }
    var totalRegistros by remember { mutableStateOf(0) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Control de Gastos",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Registra compras individuales al instante para evitar gastos hormiga.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        AssistChip(
            onClick = { },
            label = { Text("Gastos anotados en sesión: $totalRegistros") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = concepto,
            onValueChange = {
                concepto = it
                if (esError) esError = false
            },
            label = { Text("Concepto del gasto (ej. Almuerzo, Café)") },
            modifier = Modifier.fillMaxWidth(),
            isError = esError && concepto.isBlank(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = monto,
            onValueChange = {
                monto = it
                if (esError) esError = false
            },
            label = { Text("Monto gastado ($)") },
            modifier = Modifier.fillMaxWidth(),
            isError = esError && monto.isBlank(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (concepto.isBlank() || monto.isBlank()) {
                        mensaje = "Advertencia: Ingresa tanto el concepto como el monto gastado."
                        esError = true
                    } else {
                        totalRegistros++
                        mensaje = "Gasto #$totalRegistros registrado con éxito:\n" +
                                "• Concepto: $concepto\n" +
                                "• Monto: $$monto\n" +
                                "• Categoría: $categoria\n" +
                                "• Estado: Guardado en sesión local"
                        esError = false
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Registrar Gasto")
            }

            OutlinedButton(
                onClick = {
                    concepto = ""
                    monto = ""
                    categoria = "Comida"
                    mensaje = "Aún no hay gastos registrados"
                    esError = false
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Limpiar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                modifier = Modifier.padding(16.dp),
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
