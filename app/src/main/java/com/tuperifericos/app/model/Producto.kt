package com.tuperifericos.app.model

/** Categorías de periféricos de la tienda. Propuesta del equipo, ajustable. */
enum class CategoriaProducto(val etiqueta: String) {
    TECLADOS("Teclados"),
    MOUSE("Mouse"),
    AUDIFONOS("Audífonos y micrófonos"),
    MONITORES("Monitores"),
    WEBCAMS("Cámaras web"),
    ACCESORIOS("Accesorios y cables"),
    OTRO("Otro")
}

enum class UnidadMedida(val etiqueta: String) {
    UNIDAD("Unidad"),
    CAJA("Caja"),
    PACK("Pack"),
    PAR("Par"),
    KIT("Kit")
}

/** Estado del producto: pasa a Agotado solo cuando el saldo llega a 0 (ver Repository). */
enum class EstadoProducto(val etiqueta: String) {
    DISPONIBLE("Disponible"),
    AGOTADO("Agotado"),
    DESCONTINUADO("Descontinuado")
}

/**
 * Periférico de la tienda (teclado, mouse, audífonos, etc.).
 *
 * cantidadDisponible NO se edita a mano; se deriva de los movimientos de ingreso y salida
 * (ver InventarioRepository.registrarMovimiento()).
 *
 * Persistencia: en Room se guarda como ProductoEntity (data/local/ProductoEntity.kt).
 */
data class Producto(
    val id: String,
    val codigo: String,
    val nombre: String,
    val descripcion: String,
    val categoria: CategoriaProducto,
    val unidadMedida: UnidadMedida,
    val cantidadDisponible: Int = 0,
    val nivelMinimo: Int,
    val ubicacion: String,
    val estado: EstadoProducto = EstadoProducto.DISPONIBLE
)

