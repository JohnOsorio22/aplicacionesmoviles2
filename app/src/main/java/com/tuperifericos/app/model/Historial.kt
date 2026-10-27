package com.tuperifericos.app.model

/** Filtro por tipo de movimiento en la pantalla de historial (RF-10). */
enum class FiltroHistorial(val etiqueta: String) {
    TODOS("Todos"),
    INGRESOS("Ingresos"),
    SALIDAS("Salidas")
}

/** Un movimiento junto con el saldo que quedó del producto justo después de él. */
data class MovimientoHistorial(
    val movimiento: Movimiento,
    val saldoResultante: Int
)

data class ResumenHistorial(
    val totalIngresos: Int = 0,
    val totalSalidas: Int = 0
) {
    val saldo: Int get() = totalIngresos - totalSalidas
}

/** Estado completo que necesita la pantalla de historial. */
data class HistorialUiState(
    val movimientos: List<MovimientoHistorial> = emptyList(),
    val resumen: ResumenHistorial = ResumenHistorial(),
    val totalSinFiltrar: Int = 0
)
