# StoreViewModel único, pedido e inventario

## Resumen

- Expone un único `StateFlow<StoreUiState>` inmutable desde `StoreViewModel`.
- Mantiene catálogo completo, consulta, pedido, mensajes y posición de ambas vistas del catálogo en el estado observable.
- Delega las reglas de pedido a funciones puras, sin dependencia de Compose.
- Conserva el estado al rotar porque el `StoreViewModel` está asociado a la actividad y las posiciones se sincronizan con él.
- Usa importes en centavos y un único formateador USD para precios, subtotales y total.

## Responsabilidades

| Clasificación | Responsabilidad | Implementación |
|---|---|---|
| Modelo | Representar productos, perfiles y una línea del pedido sin lógica de UI. | `GameProduct`, `DeveloperProfile`, `OrderLine` |
| Regla de negocio | Agregar, acumular, quitar y cambiar cantidades; validar producto, cantidad y existencias; calcular subtotales/total y formatear moneda. | `OrderOperations.kt`: `addToOrder`, `removeFromOrder`, `updateQuantity`, `calculateSubtotal`, `calculateTotal`, `formatCurrency` |
| Estado de pantalla | Ser la única fuente observable para catálogo, consulta, pedido, feedback y posición; derivar resultados filtrados y resumen del pedido. | `StoreUiState` + `StoreViewModel.uiState: StateFlow<StoreUiState>` |
| Estado visual | Controlar únicamente decisiones efímeras de presentación que no afectan al negocio. | `renderMode` y expansión de ficha técnica con `rememberSaveable`; estados de scroll sincronizados con `StoreViewModel` |

## Catálogo local de prueba

Confirmado: se generan localmente exactamente **500 productos** en `TestCatalog.createProducts()`.

- IDs deterministas desde `game-001` hasta `game-500`.
- Datos reproducibles: nombre, género, precio, existencias y color de portada.
- Los 10 perfiles existentes se conservan y cada uno queda asociado con 50 productos.
- No se requieren imágenes ni datos remotos; las portadas se dibujan localmente.
- Hay productos con y sin existencias para cubrir los casos de validación.

## Validaciones cubiertas

- Producto inexistente.
- Producto agotado.
- Cantidad menor o igual a cero.
- Cantidad que supera existencias.
- Actualización o eliminación de una línea inexistente.
- Acumulación de varias adiciones en una sola línea por producto.
- Consolidación defensiva de líneas duplicadas preexistentes.

## Verificación

- `./gradlew test`
- `./gradlew assembleDebug`
- Rotación manual sugerida: buscar, agregar productos, desplazar ambas vistas y rotar; verificar que catálogo, consulta, pedido y posición permanezcan.
