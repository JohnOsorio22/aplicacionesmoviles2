package com.tuperifericos.app.ui.screen.productos

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tuperifericos.app.navigation.Rutas
import com.tuperifericos.app.ui.components.BarraSuperior
import com.tuperifericos.app.viewmodel.TuPerifericosViewModel

/**
 * Catálogo de periféricos con búsqueda por nombre o código. Tocar un producto abre su detalle,
 * desde donde se registra un movimiento o se ve su historial (RF-10).
 */
@Composable
fun ProductosScreen(navController: NavHostController, viewModel: TuPerifericosViewModel) {
    // Se observa la lista para que la pantalla se redibuje cuando cambie.
    val todos by viewModel.productos.collectAsState()
    var texto by remember { mutableStateOf("") }
    val resultado = remember(todos, texto) { viewModel.buscarProductos(texto) }

    Scaffold(
        topBar = { BarraSuperior("Productos", navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text("Buscar por nombre o código") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            if (resultado.isEmpty()) {
                Text(
                    "No hay productos para mostrar.",
                    modifier = Modifier.padding(top = 24.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(resultado, key = { it.id }) { producto ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .clickable {
                                viewModel.seleccionarProducto(producto.id)
                                navController.navigate(Rutas.PRODUCTO_DETALLE)
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                            Text("${producto.codigo} · ${producto.categoria.etiqueta}", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "Disponible: ${producto.cantidadDisponible} ${producto.unidadMedida.etiqueta.lowercase()}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text("Estado: ${producto.estado.etiqueta}", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
