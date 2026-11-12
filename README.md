# Registro de Incidencias — Control de Gastos Personales

Aplicación móvil desarrollada en Android Studio con Jetpack Compose para la asignatura Técnicas de Producción Industrial de Software I (Universidad Tecnológica de El Salvador).

## Propósito del Proyecto
Solución móvil orientada al registro inmediato de consumos individuales y gastos diarios. Permite documentar salidas de dinero en el momento en que ocurren, categorizarlas y llevar un control estricto para evitar pérdidas financieras por gastos hormiga.

## Avance Semana 7: Primer Avance Técnico
- Definición de problemática, perfil de usuario y requerimientos del sistema.
- Interfaz reactiva con campos validados para concepto y monto numérico.
- Selector interactivo de categorías mediante componentes `FilterChip`.
- Contador de registros activos en la sesión actual.
- Tarjeta dinámica con confirmación estructurada y botón para limpiar el formulario.

## Avance Semana 10: Teclado Contextual e Interacción Táctil
- Configuración de `KeyboardOptions` con `KeyboardCapitalization.Sentences` y acciones IME (`ImeAction.Next` e `ImeAction.Done`).
- Gestión de foco y cierre del teclado en pantalla con `LocalFocusManager` y `LocalSoftwareKeyboardController`.
- Interacción táctil de alto nivel con `Modifier.clickable` para la selección interactiva del método de pago.
- Retroalimentación dinámica en tiempo real tras eventos táctiles y de escritura.

## Avance Semana 11: Integración Técnica con Sensores (Acelerómetro)
- Detección de hardware y disponibilidad mediante `SensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)`.
- Ciclo de vida seguro en Jetpack Compose gestionado con `DisposableEffect` para vincular (`registerListener`) y liberar (`unregisterListener`) el sensor de memoria.
- Procesamiento continuo de aceleración vectorial en ejes X, Y y Z en m/s² con tasa de refresco `SENSOR_DELAY_UI`.
- Interpretación dinámica de estabilidad y cálculo euclidiano para clasificar estados: reposo, movimiento moderado o sacudida brusca.
- Ajuste de interfaz con `statusBarsPadding()` y `navigationBarsPadding()` para compatibilidad visual con bordes de pantalla y cámara frontal.

- ## Avance Semana 12: Práctica Integradora - Tablero de Diagnóstico
- Integración de sección visible de diagnóstico de hardware en tiempo real.
- Verificación preventiva de disponibilidad del acelerómetro físico (`Sensor.TYPE_ACCELEROMETER`).
- Control estricto del ciclo de vida en Jetpack Compose mediante `DisposableEffect` para registrar (`registerListener`) y liberar recursos (`unregisterListener`).
- Interpretación contextual del movimiento mediante cálculo de magnitud euclidiana (reposo estable, movimiento moderado o sacudida brusca).

- ## Avance Semana 14: Bitácora del Ciclo de Vida
- Instrumentación de callbacks del ciclo de vida (`onCreate`, `onStart`, `onResume`, `onPause`, `onStop`, `onDestroy`) en `MainActivity`.
- Depuración y monitoreo de estados en Logcat utilizando el tag `TPIS1_LIFECYCLE`.
- Registro y análisis de secuencias reales: inicio en frío, suspensión/retorno con Home y finalización de proceso por el sistema (`PROCESS ENDED`).

- ## Avance Semana 15: Conservación del Estado en Formulario
- **Objetivo:** Comprobar cómo responde el estado de la interfaz cuando Android recrea `MainActivity` ante una rotación de pantalla.
- **Cambios en `MainActivity.kt`:**
  - Sustitución de `remember` por `rememberSaveable` en `concepto` para conservar su contenido en el `Bundle` del sistema.
  - Conservación de `remember` en `monto` para comparar ambos mecanismos en una misma pantalla.
  - Límite de frecuencia en el acelerómetro (350 ms) y `.imePadding()` en el contenedor para mantener estable la escritura.
- **Resultado comprobado:** Al girar el emulador Pixel 10 Pro XL (API 37.1), `concepto` conservó el dato ingresado y `monto` se reinició vacío.

- ## Avance Semana 16: Estado de Pantalla con ViewModel
- **Objetivo:** Separar el estado del formulario y sus eventos fuera del composable mediante `RegistroIncidenciasViewModel`.
- **Estado trasladado al ViewModel:** `concepto`, `monto`, `categoria`, `metodoPago`, `mensaje`, `esError` y `totalRegistros` con propiedades `private set` y funciones de evento (`actualizarConcepto`, `actualizarMonto`, `seleccionarCategoria`, `seleccionarMetodoPago`, `registrarGasto`, `limpiarFormulario`).
- **Estado conservado en la UI:** Desplazamiento visual (`rememberScrollState()`), controladores de foco/teclado y lecturas instantáneas del acelerómetro.
- **Resultado comprobado:** Al rotar el emulador, tanto los textos ingresados como las selecciones activas se conservaron intactos sin depender de `rememberSaveable`.
