package com.tuperifericos.app.model

/**
 * Usuario que inicia sesión en la app. Su nombre queda registrado como "responsable" de cada
 * movimiento de inventario (RF del módulo "Movimientos de inventario").
 *
 * Solo datos ficticios (ver MockData.usuarios).
 */
data class Usuario(
    val id: String,
    val nombre: String,
    val usuario: String,
    val clave: String,
    val perfil: Perfil
)

/** Errores de validación del formulario de login (null = sin error). */
data class ErroresLogin(
    val usuario: String? = null,
    val clave: String? = null,
    val general: String? = null
) {
    val hayErrores: Boolean
        get() = usuario != null || clave != null || general != null
}
