
package com.tuperifericos.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tuperifericos.app.model.MotivoSalida
import com.tuperifericos.app.model.Movimiento
import com.tuperifericos.app.model.TipoMovimiento

@Entity(
    tableName = "movimientos",
    foreignKeys = [
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("productoId")]
)
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productoId: String,
    val fecha: String,
    val tipo: String,
    val cantidad: Int,
    val motivo: String?,
    val observacion: String,
    val responsable: String
)

fun MovimientoEntity.toModel() = Movimiento(
    id = id.toString(),
    productoId = productoId,
    fecha = fecha,
    tipo = TipoMovimiento.valueOf(tipo),
    cantidad = cantidad,
    motivo = motivo?.let { MotivoSalida.valueOf(it) },
    observacion = observacion,
    responsable = responsable
)

fun Movimiento.toEntity() = MovimientoEntity(
    id = id.toLongOrNull() ?: 0,
    productoId = productoId,
    fecha = fecha,
    tipo = tipo.name,
    cantidad = cantidad,
    motivo = motivo?.name,
    observacion = observacion,
    responsable = responsable
)
