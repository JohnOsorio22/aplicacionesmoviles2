package com.tuperifericos.app.ui.screen.movimientos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tuperifericos.app.model.ErroresMovimiento
import com.tuperifericos.app.model.MotivoSalida
import com.tuperifericos.app.model.Producto
import com.tuperifericos.app.model.TipoMovimiento
import com.tuperifericos.app.ui.components.BarraSuperior
import com.tuperifericos.app.ui.components.SelectorDesplegable
import com.tuperifericos.app.viewmodel.TuPerifericosViewModel

/**
 * Módulo "Movimientos de inventario": registrar un ingreso (recepción de mercadería) o una
 * salida (venta, merma, etc.).
 *
 * La validación y el registro están en el ViewModel. La fecha y el responsable no se piden: los
 * asigna el ViewModel al registrar (fecha actual y usuario con sesión iniciada). Antes de
 * registrar una salida se pide confirmación.
 */
@Composable
fun RegistrarMovimientoScreen(navController: NavHostController, viewModel: TuPerifericosViewModel) {
    val productos by viewModel.productos.collectAsState()
    val seleccionadoId by viewModel.productoSeleccionadoId.collectAsState()
    val usuario by viewModel.usuarioActivo.collectAsState()

    var producto by remember { mutableStateOf(productos.find { it.id == seleccionadoId }) }
    var tipo by remember { mutableStateOf(TipoMovimiento.INGRESO) }
    var cantidad by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf<MotivoSalida?>(null) }
    var observacion by remember { mutableStateOf("") }
    var errores by remember { mutableStateOf(ErroresMovimiento()) }
    var confirmarSalida by remember { mutableStateOf(false) }

    val registrar: () -> Unit = {
        val elegido = producto
        if (elegido != null) {
            viewModel.registrarMovimiento(
                productoId = elegido.id,
                tipo = tipo,
                cantidad = cantidad.toIntOrNull() ?: 0,
                motivo = if (tipo == TipoMovimiento.SALIDA) motivo else null,
                observacion = observacion.trim()
            )
            navController.popBackStack()
        }
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("Confirmar salida") },
            text = {
                Text(
                    "Se descontarán $cantidad ${producto?.unidadMedida?.etiqueta?.lowercase().orEmpty()} " +
                        "de ${producto?.nombre.orEmpty()} (motivo: ${motivo?.etiqueta.orEmpty()}). ¿Continuar?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    registrar()
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(topBar = { BarraSuperior("Registrar movimiento", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SelectorDesplegable<Producto>(
                etiqueta = "Producto",
                opciones = productos,
                seleccion = producto,
                textoDe = { "${it.codigo} · ${it.nombre}" },
                onSeleccion = { producto = it },
                error = errores.producto
            )
            producto?.let { elegido ->
                // Se muestra la cantidad más reciente del producto elegido.
                val actual = productos.find { it.id == elegido.id } ?: elegido
                Text(
                    "Disponible actualmente: ${actual.cantidadDisponible} ${actual.unidadMedida.etiqueta.lowercase()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text("Tipo de movimiento", style = MaterialTheme.typography.titleSmall)
            TipoMovimiento.entries.forEach { opcion ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = tipo == opcion, onClick = { tipo = opcion })
                    Text(opcion.etiqueta)
                }
            }

            OutlinedTextField(
                value = cantidad,
                onValueChange = { cantidad = it },
                label = { Text("Cantidad") },
                singleLine = true,
                isError = errores.cantidad != null,
                supportingText = if (errores.cantidad != null) {
                    { Text(errores.cantidad.orEmpty()) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(visible = tipo == TipoMovimiento.SALIDA) {
                SelectorDesplegable(
                    etiqueta = "Motivo de la salida",
                    opciones = MotivoSalida.entries,
                    seleccion = motivo,
                    textoDe = { it.etiqueta },
                    onSeleccion = { motivo = it },
                    error = errores.motivo
                )
            }

            OutlinedTextField(
                value = observacion,
                onValueChange = { observacion = it },
                label = { Text("Observación (opcional)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "Responsable: ${usuario?.nombre.orEmpty()} · La fecha se registra automáticamente.",
                style = MaterialTheme.typography.bodySmall
            )

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                onClick = {
                    errores = viewModel.validarMovimiento(producto?.id, tipo, cantidad, motivo)
                    if (!errores.hayErrores && producto != null) {
                        if (tipo == TipoMovimiento.SALIDA) confirmarSalida = true else registrar()
                    }
                }
            ) { Text("Registrar movimiento") }
        }
    }
}
