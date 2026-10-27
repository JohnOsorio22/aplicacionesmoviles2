package com.tuperifericos.app.model

/** Perfiles de la tienda: quien administra el inventario y quien atiende ventas. */
enum class Perfil(val etiqueta: String) {
    ENCARGADO("Encargado de tienda"),
    USUARIO_AUTORIZADO("Vendedor")
}
