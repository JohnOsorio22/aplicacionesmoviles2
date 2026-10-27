package com.tuperifericos.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tuperifericos.app.data.InventarioRepository
import com.tuperifericos.app.data.local.AppDatabase
import com.tuperifericos.app.model.ErroresLogin
import com.tuperifericos.app.model.ErroresMovimiento
import com.tuperifericos.app.model.FiltroHistorial
import com.tuperifericos.app.model.HistorialUiState
import com.tuperifericos.app.model.MotivoSalida
import com.tuperifericos.app.model.Movimiento
import com.tuperifericos.app.model.MovimientoHistorial
import com.tuperifericos.app.model.Producto
import com.tuperifericos.app.model.ResumenHistorial
import com.tuperifericos.app.model.TipoMovimiento
import com.tuperifericos.app.model.Usuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

/**
 * ViewModel único, compartido por todas las pantallas (se crea una sola vez en AppNavigation).
 *
 * Arquitectura: Pantalla (Compose) -> ViewModel -> InventarioRepository -> DAO -> Room.
 * Las pantallas solo muestran estado (StateFlow + collectAsState) y avisan eventos; las
 * validaciones y reglas de negocio viven aquí.
 */
class TuPerifericosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventarioRepository(AppDatabase.obtener(application))

    init {
        // Primera ejecución: carga los datos ficticios en la base de datos.
        viewModelScope.launch { repository.cargarDatosDeEjemploSiVacio() }
    }

    // =====================================================================
    // Mensajes de éxito (se muestran como Snackbar en AppNavigation)
    // =====================================================================

    private val _mensajes = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    private fun avisar(texto: String) {
        _mensajes.tryEmit(texto)
    }

    // =====================================================================
    // Autenticación
    // =====================================================================

    private val _usuarioActivo = MutableStateFlow<Usuario?>(null)
    val usuarioActivo: StateFlow<Usuario?> = _usuarioActivo.asStateFlow()

    private val _erroresLogin = MutableStateFlow(ErroresLogin())
    val erroresLogin: StateFlow<ErroresLogin> = _erroresLogin.asStateFlow()

    /**
     * Valida los campos (vacíos y formato de correo) y luego las credenciales contra la tabla
     * de usuarios. Si son correctas deja el usuario activo y llama a [onExito].
     */
    fun login(usuario: String, clave: String, onExito: () -> Unit) {
        val errores = ErroresLogin(
            usuario = when {
                usuario.isBlank() -> "Ingresa tu usuario"
                !REGEX_CORREO.matches(usuario) -> "Ingresa un correo válido"
                else -> null
            },
            clave = if (clave.isBlank()) "Ingresa tu clave" else null
        )
        _erroresLogin.value = errores
        if (errores.hayErrores) return

        viewModelScope.launch {
            val encontrado = repository.iniciarSesion(usuario, clave)
            if (encontrado == null) {
                _erroresLogin.value = ErroresLogin(general = "Usuario o clave incorrectos")
            } else {
                _usuarioActivo.value = encontrado
                onExito()
            }
        }
    }

    fun logout() {
        _usuarioActivo.value = null
        _erroresLogin.value = ErroresLogin()
        _productoSeleccionadoId.value = null
    }

    // =====================================================================
    // Productos (solo consulta: sirven para elegir de cuál ver el historial)
    // =====================================================================

    // Eagerly: productoPorId(), buscarProductos() y las validaciones leen .value fuera de una
    // pantalla que observe el flujo; con WhileSubscribed devolverían la lista vacía inicial.
    val productos: StateFlow<List<Producto>> = repository.productos
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Producto elegido en una lista (para abrir su detalle, editarlo o ver su historial). */
    private val _productoSeleccionadoId = MutableStateFlow<String?>(null)
    val productoSeleccionadoId: StateFlow<String?> = _productoSeleccionadoId.asStateFlow()

    fun seleccionarProducto(id: String?) {
        _productoSeleccionadoId.value = id
        _filtroHistorial.value = FiltroHistorial.TODOS
    }

    fun productoPorId(id: String?): Producto? = productos.value.find { it.id == id }

    /** Busca por nombre o código, sin distinguir mayúsculas ni espacios al inicio o al final. */
    fun buscarProductos(texto: String): List<Producto> {
        val consulta = texto.trim()
        return productos.value.filter { producto ->
            consulta.isEmpty() ||
                producto.nombre.contains(consulta, ignoreCase = true) ||
                producto.codigo.contains(consulta, ignoreCase = true)
        }
    }

    // =====================================================================
    // Registro de movimientos (alimenta el historial)
    // =====================================================================

    val movimientos: StateFlow<List<Movimiento>> = repository.movimientos
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Valida el formulario de movimiento: producto elegido, cantidad numérica mayor que 0,
     * motivo obligatorio en las salidas y que una salida no supere la cantidad disponible.
     */
    fun validarMovimiento(
        productoId: String?,
        tipo: TipoMovimiento,
        cantidad: String,
        motivo: MotivoSalida?
    ): ErroresMovimiento {
        val producto = productoPorId(productoId)
        val cantidadEntera = cantidad.toIntOrNull()
        return ErroresMovimiento(
            producto = if (producto == null) "Selecciona un producto" else null,
            cantidad = when {
                cantidad.isBlank() -> "Ingresa la cantidad"
                cantidadEntera == null -> "La cantidad debe ser un número entero"
                cantidadEntera <= 0 -> "La cantidad debe ser mayor que 0"
                tipo == TipoMovimiento.SALIDA && producto != null &&
                    cantidadEntera > producto.cantidadDisponible ->
                    "Stock insuficiente: solo hay ${producto.cantidadDisponible} disponible(s)"
                else -> null
            },
            motivo = if (tipo == TipoMovimiento.SALIDA && motivo == null) {
                "Selecciona el motivo de la salida"
            } else {
                null
            }
        )
    }

    /**
     * Registra el movimiento con la fecha actual y el usuario con sesión iniciada como
     * responsable. El Repository lo guarda y recalcula la cantidad disponible (RN-01).
     */
    fun registrarMovimiento(
        productoId: String,
        tipo: TipoMovimiento,
        cantidad: Int,
        motivo: MotivoSalida?,
        observacion: String
    ) {
        val responsable = _usuarioActivo.value?.nombre ?: return
        val producto = productoPorId(productoId) ?: return
        val movimiento = Movimiento(
            id = "",
            productoId = productoId,
            fecha = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date()),
            tipo = tipo,
            cantidad = cantidad,
            motivo = if (tipo == TipoMovimiento.SALIDA) motivo else null,
            observacion = observacion,
            responsable = responsable
        )
        viewModelScope.launch {
            val saldo = repository.registrarMovimiento(movimiento)
            avisar("Movimiento registrado. Disponible: $saldo ${producto.unidadMedida.etiqueta.lowercase()}")
        }
    }

    // =====================================================================
    // Historial
    // =====================================================================

    // ---------------------------------------------------------------------
    // RF-10: historial de ingresos y salidas por producto
    // ---------------------------------------------------------------------

    private val _filtroHistorial = MutableStateFlow(FiltroHistorial.TODOS)
    val filtroHistorial: StateFlow<FiltroHistorial> = _filtroHistorial.asStateFlow()

    fun cambiarFiltroHistorial(filtro: FiltroHistorial) {
        _filtroHistorial.value = filtro
    }

    /**
     * Historial del producto seleccionado (todos los productos si no hay ninguno), del más
     * reciente al más antiguo. Se recalcula solo cuando cambia el producto, el filtro o los
     * datos de Room (por ejemplo, al registrar un movimiento nuevo).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val historial: StateFlow<HistorialUiState> = combine(
        _productoSeleccionadoId.flatMapLatest { productoId -> repository.historial(productoId) },
        _filtroHistorial
    ) { movimientosRecientesPrimero, filtro ->
        historialDe(movimientosRecientesPrimero, filtro)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistorialUiState())

    /**
     * Arma el historial comprensible: agrega a cada movimiento el saldo que quedó del producto
     * después de él, y calcula los totales de ingresos y salidas. El saldo se calcula con TODOS
     * los movimientos y el filtro se aplica después, para que el saldo no cambie al filtrar.
     */
    private fun historialDe(
        movimientosRecientesPrimero: List<Movimiento>,
        filtro: FiltroHistorial
    ): HistorialUiState {
        val saldoPorProducto = mutableMapOf<String, Int>()
        val conSaldo: List<MovimientoHistorial> = movimientosRecientesPrimero
            .asReversed() // del más antiguo al más reciente, para ir acumulando
            .map { movimiento ->
                val variacion = if (movimiento.tipo == TipoMovimiento.INGRESO) {
                    movimiento.cantidad
                } else {
                    -movimiento.cantidad
                }
                val saldo = (saldoPorProducto[movimiento.productoId] ?: 0) + variacion
                saldoPorProducto[movimiento.productoId] = saldo
                MovimientoHistorial(movimiento, saldo)
            }
            .asReversed() // otra vez el más reciente primero

        val filtrados = when (filtro) {
            FiltroHistorial.TODOS -> conSaldo
            FiltroHistorial.INGRESOS -> conSaldo.filter { it.movimiento.tipo == TipoMovimiento.INGRESO }
            FiltroHistorial.SALIDAS -> conSaldo.filter { it.movimiento.tipo == TipoMovimiento.SALIDA }
        }

        val resumen = ResumenHistorial(
            totalIngresos = movimientosRecientesPrimero
                .filter { it.tipo == TipoMovimiento.INGRESO }
                .sumOf { it.cantidad },
            totalSalidas = movimientosRecientesPrimero
                .filter { it.tipo == TipoMovimiento.SALIDA }
                .sumOf { it.cantidad }
        )
        return HistorialUiState(filtrados, resumen, movimientosRecientesPrimero.size)
    }

    /**
     * Texto plano del historial que se está viendo (producto elegido o todos, con el filtro
     * aplicado). Lo usa el recurso nativo "compartir" de HistorialMovimientosScreen.
     */
    fun textoParaCompartir(): String {
        val producto = productoPorId(_productoSeleccionadoId.value)
        val estado = historial.value
        val titulo = if (producto != null) "Historial de ${producto.nombre} (${producto.codigo})" else "Historial de movimientos"
        val lineas = estado.movimientos.map { item ->
            val m = item.movimiento
            val signo = if (m.tipo == TipoMovimiento.INGRESO) "+" else "-"
            val detalle = if (m.tipo == TipoMovimiento.INGRESO) "Ingreso" else "Salida (${m.motivo?.etiqueta ?: "Sin motivo"})"
            val nombre = if (producto == null) "${productoPorId(m.productoId)?.nombre ?: m.productoId} · " else ""
            "${m.fecha} · $nombre$signo${m.cantidad} · $detalle · saldo ${item.saldoResultante} · ${m.responsable}"
        }
        return buildString {
            appendLine("TuPerifericos — $titulo")
            appendLine("Ingresos: ${estado.resumen.totalIngresos} · Salidas: ${estado.resumen.totalSalidas}")
            lineas.forEach { appendLine(it) }
        }.trim()
    }
}
