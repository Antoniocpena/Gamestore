# Plan para arreglar CatalogScreen.kt

Se han detectado múltiples errores de compilación (referencias no resueltas), código incompleto y advertencias en `CatalogScreen.kt`. Además, la barra de búsqueda no es funcional debido a que el estado no está conectado correctamente.

## Cambios Propuestos

### [Componente] UI de Catálogo

#### [MODIFICAR] [CatalogScreen.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/Gamestore2/app/src/main/java/com/example/gamestore/ui/screens/CatalogScreen.kt)
- **Corrección de Imports**: Añadir todas las dependencias faltantes de Coil, Compose Animation y Material3.
- **Completar `ImageSkeleton`**: Finalizar la implementación de la animación de carga que quedó cortada.
- **Conectar Búsqueda**:
    - Añadir parámetro `searchQuery: String` a la función `CatalogScreen`.
    - Actualizar `TextField` para mostrar `searchQuery`.
    - Añadir un botón para limpiar la búsqueda usando `onClearQuery`.
- **Limpieza de código**:
    - Corregir advertencias de parámetros no usados.
    - Aplicar formato idiomático de Kotlin (trailing commas, trailing lambdas).

#### [MODIFICAR] [StoreUiState.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/Gamestore2/app/src/main/java/com/example/gamestore/ui/state/StoreUiState.kt)
- Añadir el campo `searchQuery: String` para mantener el estado de la búsqueda de forma centralizada.

#### [MODIFICAR] [StoreViewModel.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/Gamestore2/app/src/main/java/com/example/gamestore/StoreViewModel.kt)
- Implementar la lógica de filtrado de productos basándose en la `searchQuery`.
- Añadir funciones `updateSearchQuery` y `clearSearchQuery`.

#### [MODIFICAR] [MainActivity.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/Gamestore2/app/src/main/java/com/example/gamestore/MainActivity.kt)
- Pasar `uiState.searchQuery` a `CatalogScreen`.
- Conectar los callbacks de búsqueda con el `StoreViewModel`.

## Plan de Verificación

### Pruebas Manuales
- Verificar que el proyecto compile sin errores.
- Confirmar que la barra de búsqueda permite escribir y filtra los resultados.
- Comprobar que el botón "Limpiar" borra el texto de búsqueda.
- Validar que las imágenes muestran el skeleton animado mientras cargan.
- Asegurar que el cambio entre modo "Lazy Grid" y "Convencional" sigue funcionando y se loguea correctamente.
