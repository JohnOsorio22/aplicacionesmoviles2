package com.tuperifericos.app.ui.screen.movimientos

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tuperifericos.app.model.FiltroHistorial
import com.tuperifericos.app.model.MovimientoHistorial
import com.tuperifericos.app.model.Producto
import com.tuperifericos.app.model.TipoMovimiento
import com.tuperifericos.app.ui.components.BarraSuperior
import com.tuperifericos.app.viewmodel.TuPerifericosViewModel

/**
 * RF-10: historial comprensible de ingresos y salidas. Si se llega desde el detalle de un
 * producto muestra solo ese producto (con su disponibilidad y totales); desde el panel muestra
 * los movimientos de todos. Cada movimiento indica el saldo que quedó después de él.
 *
 * Toda la lógica (orden, saldo, totales, filtro) está en TuPerifericosViewModel.historial.
 */
@Composable
fun HistorialMovimientosScreen(navController: NavHostController, viewModel: TuPerifericosViewModel) {
    val seleccionadoId by viewModel.productoSeleccionadoId.collectAsState()
    val productos by viewModel.productos.collectAsState()
    val estado by viewModel.historial.collectAsState()
    val filtro by viewModel.filtroHistorial.collectAsState()
    val context = LocalContext.current

    val producto = productos.find { it.id == seleccionadoId }
    val titulo = if (producto != null) "Historial: ${producto.nombre}" else "Historial de movimientos"

    Scaffold(topBar = { BarraSuperior(titulo, navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            if (producto != null) {
                ResumenProducto(
                    producto = producto,
                    totalIngresos = estado.resumen.totalIngresos,
                    totalSalidas = estado.resumen.totalSalidas
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                FiltroHistorial.entries.forEach { opcion ->
                    FilterChip(
                        selected = filtro == opcion,
                        onClick = { viewModel.cambiarFiltroHistorial(opcion) },
                        label = { Text(opcion.etiqueta) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${estado.movimientos.size} de ${estado.totalSinFiltrar} movimiento(s)",
                    style = MaterialTheme.typography.labelMedium
                )
                // Recurso nativo: menú "Compartir" de Android (Intent ACTION_SEND, sin permisos).
                OutlinedButton(
                    enabled = estado.movimientos.isNotEmpty(),
                    onClick = {
                        val envio = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Historial TuPerifericos")
                            putExtra(Intent.EXTRA_TEXT, viewModel.textoParaCompartir())
                        }
                        context.startActivity(Intent.createChooser(envio, "Compartir historial"))
                    }
                ) { Text("Compartir") }
            }

            if (estado.movimientos.isEmpty()) {
                Text(
                    text = if (estado.totalSinFiltrar == 0) {
                        "Todavía no hay movimientos registrados."
                    } else {
                        "No hay movimientos de este tipo."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(estado.movimientos, key = { it.movimiento.id }) { item ->
                    TarjetaMovimiento(
                        item = item,
                        producto = productos.find { it.id == item.movimiento.productoId },
                        mostrarProducto = producto == null
                    )
                }
            }
        }
    }
}

@Composable
private fun ResumenProducto(producto: Producto, totalIngresos: Int, totalSalidas: Int) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Disponible ahora: ${producto.cantidadDisponible} ${producto.unidadMedida.etiqueta.lowercase()}",
                style = MaterialTheme.typography.titleMedium
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total ingresado: +$totalIngresos", color = MaterialTheme.colorScheme.primary)
                Text("Total salidas: −$totalSalidas", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun TarjetaMovimiento(item: MovimientoHistorial, producto: Producto?, mostrarProducto: Boolean) {
    val movimiento = item.movimiento
    val esIngreso = movimiento.tipo == TipoMovimiento.INGRESO
    val signo = if (esIngreso) "+" else "−"
    val color = if (esIngreso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val unidad = producto?.unidadMedida?.etiqueta?.lowercase().orEmpty()
    val descripcionTipo = if (esIngreso) {
        "Ingreso (llegada de mercadería)"
    } else {
        "Salida · ${movimiento.motivo?.etiqueta ?: "Sin motivo"}"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "$signo${movimiento.cantidad}",
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (mostrarProducto) {
                    Text(
                        producto?.nombre ?: movimiento.productoId,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Text(descripcionTipo, style = MaterialTheme.typography.bodyMedium)
                if (movimiento.observacion.isNotBlank()) {
                    Text(movimiento.observacion, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "${formatearFecha(movimiento.fecha)} · ${movimiento.responsable}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Saldo", style = MaterialTheme.typography.labelSmall)
                Text(
                    "${item.saldoResultante} $unidad".trim(),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

/** "2026-10-01 09:15" -> "01-10-2026 09:15". Si el formato no calza, devuelve el texto tal cual. */
private fun formatearFecha(fecha: String): String {
    val partes = fecha.split(" ")
    val dma = partes.getOrNull(0)?.split("-")
    if (dma == null || dma.size != 3) return fecha
    val hora = partes.getOrNull(1)?.let { " $it" }.orEmpty()
    return "${dma[2]}-${dma[1]}-${dma[0]}$hora"
}
