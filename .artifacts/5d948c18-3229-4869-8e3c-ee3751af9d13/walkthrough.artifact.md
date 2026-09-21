# Corrección de advertencias (Warnings) en Pantallas y MainActivity

Se han corregido múltiples advertencias detectadas por el analizador de código en las pantallas del catálogo, detalle, perfil y en la actividad principal.

## Cambios Realizados

### Correcciones Generales
- **Trailing Commas**: Se añadieron comas finales en listas de parámetros y llamadas a funciones para cumplir con el estilo de código idiomático de Kotlin y evitar advertencias del linter.
- **Trailing Lambdas**: Se movieron los argumentos de función que son lambdas fuera de los paréntesis cuando era el último parámetro, mejorando la legibilidad.
- **Boolean Literals**: Se añadieron nombres de parámetros a los argumentos booleanos literales (ej. `value = true`, `enable = true`) para mayor claridad y cumplimiento de las reglas de estilo.

### Mejoras Específicas
- **Deprecaciones**: Se reemplazó el uso de `Icons.Default.ArrowBack` por `Icons.AutoMirrored.Filled.ArrowBack` en las pantallas de Detalle y Perfil, asegurando la compatibilidad con sistemas que leen de derecha a izquierda (RTL).
- **Código Limpio**: Se reemplazaron parámetros de lambdas no utilizados por el guion bajo `_` en `MainActivity.kt`.
- **Imports**: Se eliminaron imports no utilizados en `DetailScreen.kt` y `ProfileScreen.kt`.

## Verificación
- Se ejecutó `analyze_file` en todos los archivos modificados, confirmando la eliminación de las advertencias de deprecación, parámetros no usados y la mayoría de las advertencias de formato.
- El proyecto mantiene su funcionalidad original sin errores de compilación.

render_diffs(file:///C:/Users/USUARIO/AndroidStudioProjects/Gamestore2/app/src/main/java/com/example/gamestore/MainActivity.kt)
render_diffs(file:///C:/Users/USUARIO/AndroidStudioProjects/Gamestore2/app/src/main/java/com/example/gamestore/ui/screens/CatalogScreen.kt)
render_diffs(file:///C:/Users/USUARIO/AndroidStudioProjects/Gamestore2/app/src/main/java/com/example/gamestore/ui/screens/DetailScreen.kt)
render_diffs(file:///C:/Users/USUARIO/AndroidStudioProjects/Gamestore2/app/src/main/java/com/example/gamestore/ui/screens/ProfileScreen.kt)
