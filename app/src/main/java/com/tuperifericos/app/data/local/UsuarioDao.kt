
package com.tuperifericos.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios WHERE LOWER(usuario) = LOWER(:usuario) AND clave = :clave LIMIT 1")
    suspend fun buscarPorCredenciales(usuario: String, clave: String): UsuarioEntity?

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Insert
    suspend fun insertarTodos(usuarios: List<UsuarioEntity>)
}
