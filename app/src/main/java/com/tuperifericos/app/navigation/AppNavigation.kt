package com.tuperifericos.app.navigation
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tuperifericos.app.ui.screen.LoginScreen
import com.tuperifericos.app.ui.screen.PanelScreen
import com.tuperifericos.app.ui.screen.movimientos.HistorialMovimientosScreen
import com.tuperifericos.app.ui.screen.movimientos.RegistrarMovimientoScreen
import com.tuperifericos.app.ui.screen.productos.ProductoDetalleScreen
import com.tuperifericos.app.ui.screen.productos.ProductosScreen
import com.tuperifericos.app.viewmodel.TuPerifericosViewModel

object Rutas {
    const val LOGIN = "login"
    const val PANEL = "panel"
    const val PRODUCTOS = "productos"
    const val PRODUCTO_DETALLE = "producto_detalle"
    const val MOVIMIENTO = "movimiento"
    const val HISTORIAL = "historial"
}

/**
 * Flujo de pantallas (un solo NavHost y un único TuPerifericosViewModel compartido):
 *
 *   LOGIN ──(login OK)──> PANEL
 *   PANEL ──> PRODUCTOS ──> PRODUCTO_DETALLE ──> MOVIMIENTO (registrar)
 *                                            └─> HISTORIAL (de ese producto)
 *   PANEL ──> MOVIMIENTO | HISTORIAL (todos los productos)
 *
 * El producto elegido se comparte a través del ViewModel (seleccionarProducto): no se pasan
 * argumentos por la ruta.
 *
 * Botón atrás: el atrás nativo de Android funciona solo (lo maneja el NavHost). Al ingresar,
 * LOGIN se saca de la pila; "Cerrar sesión" limpia la pila completa y vuelve a LOGIN.
 *
 * Los mensajes de éxito del ViewModel se muestran en un Snackbar común a todas las pantallas.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: TuPerifericosViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.mensajes.collect { mensaje -> snackbarHostState.showSnackbar(mensaje) }
    }

    val cerrarSesion: () -> Unit = {
        viewModel.logout()
        navController.navigate(Rutas.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }
    val irAlPanel: () -> Unit = {
        navController.navigate(Rutas.PANEL) {
            popUpTo(Rutas.LOGIN) { inclusive = true }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Rutas.LOGIN,
            // Transiciones entre pantallas: deslizar + desvanecer (al volver, en sentido inverso).
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) +
                        fadeIn(tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) +
                        fadeOut(tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) +
                        fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) +
                        fadeOut(tween(300))
            }
        ) {
            composable(Rutas.LOGIN) {
                LoginScreen(viewModel = viewModel, onIngresar = irAlPanel)
            }
            composable(Rutas.PANEL) {
                PanelScreen(navController = navController, viewModel = viewModel, onCerrarSesion = cerrarSesion)
            }
            composable(Rutas.PRODUCTOS) {
                ProductosScreen(navController = navController, viewModel = viewModel)
            }
            composable(Rutas.PRODUCTO_DETALLE) {
                ProductoDetalleScreen(navController = navController, viewModel = viewModel)
            }
            composable(Rutas.MOVIMIENTO) {
                RegistrarMovimientoScreen(navController = navController, viewModel = viewModel)
            }
            composable(Rutas.HISTORIAL) {
                HistorialMovimientosScreen(navController = navController, viewModel = viewModel)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }
}

