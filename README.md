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
