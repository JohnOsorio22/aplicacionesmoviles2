package com.tuperifericos.app.ui.screen

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tuperifericos.app.navigation.Rutas
import com.tuperifericos.app.ui.components.BarraSuperior
import com.tuperifericos.app.viewmodel.TuPerifericosViewModel

/**
 * Panel principal después del login: resumen y acceso a los productos, al registro de
 * movimientos y al historial (RF-10).
 */
@Composable
fun PanelScreen(
    navController: NavHostController,
    viewModel: TuPerifericosViewModel,
    onCerrarSesion: () -> Unit
) {
    val usuario by viewModel.usuarioActivo.collectAsState()
    val productos by viewModel.productos.collectAsState()
    val movimientos by viewModel.movimientos.collectAsState()

    Scaffold(topBar = { BarraSuperior("TuPerifericos", navController, onCerrarSesion) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Hola, ${usuario?.nombre.orEmpty()}", style = MaterialTheme.typography.titleLarge)
            Text("Perfil: ${usuario?.perfil?.etiqueta.orEmpty()}", style = MaterialTheme.typography.bodyMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TarjetaResumen("Productos", productos.size, Modifier.weight(1f))
                TarjetaResumen("Movimientos", movimientos.size, Modifier.weight(1f))
            }

            Button(modifier = Modifier.fillMaxWidth(), onClick = { navController.navigate(Rutas.PRODUCTOS) }) {
                Text("Productos")
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                viewModel.seleccionarProducto(null)
                navController.navigate(Rutas.MOVIMIENTO)
            }) { Text("Registrar ingreso o salida") }
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                viewModel.seleccionarProducto(null)
                navController.navigate(Rutas.HISTORIAL)
            }) { Text("Historial de movimientos") }
        }
    }
}

@Composable
private fun TarjetaResumen(titulo: String, valor: Int, modifier: Modifier = Modifier) {
    // El número "sube" animado cuando cambia (por ejemplo, al registrar un movimiento).
    val valorAnimado by animateIntAsState(
        targetValue = valor,
        animationSpec = tween(durationMillis = 600),
        label = "contador"
    )
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(valorAnimado.toString(), style = MaterialTheme.typography.headlineSmall)
            Text(titulo, style = MaterialTheme.typography.labelMedium)
        }
    }
}
