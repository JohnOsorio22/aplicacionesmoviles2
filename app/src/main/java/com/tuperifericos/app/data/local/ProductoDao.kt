
package com.tuperifericos.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    @Query("SELECT * FROM productos ORDER BY nombre COLLATE NOCASE")
    fun observarTodos(): Flow<List<ProductoEntity>>

    @Query("SELECT COUNT(*) FROM productos")
    suspend fun contar(): Int

    @Insert
    suspend fun insertarTodos(productos: List<ProductoEntity>)

    @Query("UPDATE productos SET cantidadDisponible = :cantidad WHERE id = :id")
    suspend fun actualizarCantidad(id: String, cantidad: Int)

    @Query("UPDATE productos SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: String, estado: String)

    @Query("SELECT estado FROM productos WHERE id = :id")
    suspend fun estadoDe(id: String): String?
}
