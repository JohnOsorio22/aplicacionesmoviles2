# TuPerifericos — Control de existencias de periféricos (DSY1105 · EP2)

App Android para una tienda de periféricos de computador (teclados, mouse, audífonos, monitores, cámaras web y accesorios). Se centra en **RF-10: historial comprensible de ingresos y salidas por producto**.

## Integrantes

- _Completar: nombre y apellido del integrante 1_
- _Completar: nombre y apellido del integrante 2_

## Repositorio

- _Completar: enlace al repositorio GitHub (si es privado, agregar al docente como colaborador)_

## Cómo ejecutar

1. Descomprimir el ZIP en una carpeta limpia.
2. Android Studio → **File > Open…** → seleccionar la carpeta que contiene `settings.gradle.kts`.
3. Esperar el Gradle Sync (la primera vez necesita internet). No actualizar Gradle ni el Android Gradle Plugin.
4. Ejecutar ▶ **Run 'app'** en un emulador o dispositivo (minSdk 24).
5. Si ya había una versión instalada, desinstalarla antes de ejecutar.

## Usuarios de prueba (datos ficticios)

| Perfil | Usuario | Clave |
|---|---|---|
| Encargado de tienda | encargado@tuperifericos.cl | encargado123 |
| Vendedor | ventas@tuperifericos.cl | ventas123 |

Al primer arranque se cargan en Room 8 productos, 2 usuarios y 6 movimientos de ejemplo (`data/MockData.kt`). Todos los datos son ficticios.

## Arquitectura

Kotlin · Jetpack Compose · Material 3 · Navigation Compose · MVVM (ViewModel + StateFlow) · Room (SQLite).

```
Pantalla (Compose) -> TuPerifericosViewModel -> InventarioRepository -> DAO -> Room
   ui/screen/            viewmodel/                 data/               data/local/
```

## Requerimiento implementado

| Req. | Descripción | Pantalla | ViewModel |
|---|---|---|---|
| **RF-10** | **Historial comprensible de ingresos y salidas por producto** | `HistorialMovimientosScreen` | `historial`, `historialDe()`, `textoParaCompartir()` |

Qué hace el historial:

- Desde el detalle de un producto se ve solo su historial; desde el panel, el de todos.
- Orden del más reciente al más antiguo (`MovimientoDao.observarPorProducto()` devuelve un `Flow`).
- Cada movimiento muestra cantidad con signo, tipo y motivo, observación, fecha, responsable y el **saldo que quedó después** del movimiento.
- Resumen del producto: disponible actual, total ingresado y total de salidas.
- Filtro Todos / Ingresos / Salidas (el saldo no cambia al filtrar).
- Se actualiza solo al registrar un movimiento nuevo.

Pantallas de apoyo (necesarias para que el historial tenga datos que mostrar): login, panel, lista y detalle de productos (solo lectura) y registro de ingreso/salida con validaciones. La cantidad disponible no se edita a mano: se deriva de los movimientos (`MovimientoDao.saldoDe()`), en una sola transacción.

## Recurso nativo: compartir el historial

- **Qué hace:** el botón *Compartir* del historial abre el menú de Android (`Intent.ACTION_SEND`) con el historial en texto (producto, totales y cada movimiento), para enviarlo por correo o mensajería.
- **Por qué es pertinente:** quien revisa diferencias de stock necesita enviar el historial de un producto a otra persona (por ejemplo, al proveedor o a la jefatura) sin salir de la app.
- **Restricciones:** funciona sin permisos, sin backend y sin servicios externos propios; solo usa datos ficticios.
- **Código:** `HistorialMovimientosScreen` (Intent) y `TuPerifericosViewModel.textoParaCompartir()` (arma el texto).

## Animaciones y componentes interactivos

Transiciones de navegación (deslizar + desvanecer), contadores animados en el panel, `AnimatedVisibility` (errores del login y campo de motivo), `animateContentSize` en tarjetas, diálogo de confirmación de salidas, listas desplegables, chips de filtro y Snackbar de éxito.
