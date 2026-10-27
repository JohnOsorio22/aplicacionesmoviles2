
package com.tuperifericos.app.data

import androidx.room.withTransaction
import com.tuperifericos.app.data.local.AppDatabase
import com.tuperifericos.app.data.local.toEntity
import com.tuperifericos.app.data.local.toModel
import com.tuperifericos.app.model.EstadoProducto
import com.tuperifericos.app.model.Movimiento
import com.tuperifericos.app.model.Producto
import com.tuperifericos.app.model.TipoMovimiento
import com.tuperifericos.app.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InventarioRepository(private val db: AppDatabase) {

    private val productoDao = db.productoDao()
    private val movimientoDao = db.movimientoDao()
    private val usuarioDao = db.usuarioDao()

    suspend fun iniciarSesion(usuario: String, clave: String): Usuario? =
        usuarioDao.buscarPorCredenciales(usuario, clave)?.toModel()

    val productos: Flow<List<Producto>> =
        productoDao.observarTodos().map { lista ->
            lista.map { it.toModel() }
        }

    val movimientos: Flow<List<Movimiento>> =
        movimientoDao.observarTodos().map { lista ->
            lista.map { it.toModel() }
        }

    fun historial(productoId: String?): Flow<List<Movimiento>> {
        val origen = if (productoId == null) {
            movimientoDao.observarTodos()
        } else {
            movimientoDao.observarPorProducto(productoId)
        }

        return origen.map { lista ->
            lista.map { it.toModel() }
        }
    }

    suspend fun registrarMovimiento(movimiento: Movimiento): Int =
        db.withTransaction {
            movimientoDao.insertar(movimiento.toEntity())

            val saldo = movimientoDao.saldoDe(movimiento.productoId)
            productoDao.actualizarCantidad(movimiento.productoId, saldo)

            val estado = productoDao.estadoDe(movimiento.productoId)

            if (saldo == 0 && estado == EstadoProducto.DISPONIBLE.name) {
                productoDao.actualizarEstado(
                    movimiento.productoId,
                    EstadoProducto.AGOTADO.name
                )
            } else if (saldo > 0 && estado == EstadoProducto.AGOTADO.name) {
                productoDao.actualizarEstado(
                    movimiento.productoId,
                    EstadoProducto.DISPONIBLE.name
                )
            }

            saldo
        }

    suspend fun cargarDatosDeEjemploSiVacio() {
        db.withTransaction {
            if (usuarioDao.contar() == 0) {
                usuarioDao.insertarTodos(
                    MockData.usuarios.map { it.toEntity() }
                )
            }

            if (productoDao.contar() > 0) return@withTransaction

            productoDao.insertarTodos(
                MockData.productos.map { it.toEntity() }
            )

            val stockInicial = MockData.productos.mapNotNull { producto ->
                val saldoActual = MockData.movimientos
                    .filter { it.productoId == producto.id }
                    .sumOf {
                        if (it.tipo == TipoMovimiento.INGRESO) {
                            it.cantidad
                        } else {
                            -it.cantidad
                        }
                    }

                val faltante = producto.cantidadDisponible - saldoActual

                if (faltante > 0) {
                    Movimiento(
                        id = "",
                        productoId = producto.id,
                        fecha = "2026-09-30 08:00",
                        tipo = TipoMovimiento.INGRESO,
                        cantidad = faltante,
                        observacion = "Stock inicial (datos de ejemplo)",
                        responsable = "Sistema (demo)"
                    )
                } else {
                    null
                }
            }

            movimientoDao.insertarTodos(
                (stockInicial + MockData.movimientos).map { it.toEntity() }
            )
        }
    }
}
