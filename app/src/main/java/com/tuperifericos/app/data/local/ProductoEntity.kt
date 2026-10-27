
package com.tuperifericos.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tuperifericos.app.model.CategoriaProducto
import com.tuperifericos.app.model.EstadoProducto
import com.tuperifericos.app.model.Producto
import com.tuperifericos.app.model.UnidadMedida

@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey val id: String,
    val codigo: String,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val unidadMedida: String,
    val cantidadDisponible: Int,
    val nivelMinimo: Int,
    val ubicacion: String,
    val estado: String
)

fun ProductoEntity.toModel() = Producto(
    id = id,
    codigo = codigo,
    nombre = nombre,
    descripcion = descripcion,
    categoria = CategoriaProducto.valueOf(categoria),
    unidadMedida = UnidadMedida.valueOf(unidadMedida),
    cantidadDisponible = cantidadDisponible,
    nivelMinimo = nivelMinimo,
    ubicacion = ubicacion,
    estado = EstadoProducto.valueOf(estado)
)

fun Producto.toEntity() = ProductoEntity(
    id = id,
    codigo = codigo,
    nombre = nombre,
    descripcion = descripcion,
    categoria = categoria.name,
    unidadMedida = unidadMedida.name,
    cantidadDisponible = cantidadDisponible,
    nivelMinimo = nivelMinimo,
    ubicacion = ubicacion,
    estado = estado.name
)
