package com.tuperifericos.app.model

enum class TipoMovimiento(val etiqueta: String) {
    INGRESO("Ingreso (llegada de mercadería)"),
    SALIDA("Salida")
}

/**
 * Motivo de una salida. Regla de negocio del caso: una salida por venta debe registrarse con
 * motivo VENTA. Los demás motivos son propuesta del equipo.
 */
enum class MotivoSalida(val etiqueta: String) {
    VENTA("Venta"),
    GARANTIA("Cambio por garantía"),
    DANADO("Dañado en bodega"),
    AJUSTE("Ajuste de inventario"),
    OTRO("Otro")
}

/**
 * Movimiento de inventario (base del historial de TuPerifericos).
 *
 * Persistencia: en Room se guarda como MovimientoEntity (tabla "movimientos"), relacionada con
 * "productos" por productoId (clave foránea). Es la base del historial de RF-10.
 */
data class Movimiento(
    val id: String,
    val productoId: String,
    val fecha: String,
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val motivo: MotivoSalida? = null,
    val observacion: String = "",
    val responsable: String
)

/** Errores de validación del formulario de movimiento. */
data class ErroresMovimiento(
    val producto: String? = null,
    val cantidad: String? = null,
    val motivo: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(producto, cantidad, motivo).any { it != null }
}
