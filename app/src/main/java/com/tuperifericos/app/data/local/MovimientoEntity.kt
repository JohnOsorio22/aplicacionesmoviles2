package com.tuperifericos.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movimientos")
data class MovimientoEntity(
    @PrimaryKey val id: String,
    val productoId: String,
    val tipo: String,
    val cantidad: Int,
    val fecha: Long,
    val usuarioId: String,
    val observacion: String
)
