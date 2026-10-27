
package com.tuperifericos.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tuperifericos.app.model.Perfil
import com.tuperifericos.app.model.Usuario

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["usuario"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val usuario: String,
    val clave: String,
    val perfil: String
)

fun UsuarioEntity.toModel() = Usuario(
    id = id,
    nombre = nombre,
    usuario = usuario,
    clave = clave,
    perfil = Perfil.valueOf(perfil)
)

fun Usuario.toEntity() = UsuarioEntity(
    id = id,
    nombre = nombre,
    usuario = usuario,
    clave = clave,
    perfil = perfil.name
)
