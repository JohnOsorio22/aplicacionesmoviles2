
package com.tuperifericos.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDao {

    @Query("SELECT * FROM movimientos ORDER BY fecha DESC, id DESC")
    fun observarTodos(): Flow<List<MovimientoEntity>>

    @Query("SELECT * FROM movimientos WHERE productoId = :productoId ORDER BY fecha DESC, id DESC")
    fun observarPorProducto(productoId: String): Flow<List<MovimientoEntity>>

    @Insert
    suspend fun insertar(movimiento: MovimientoEntity): Long

    @Insert
    suspend fun insertarTodos(movimientos: List<MovimientoEntity>)

    @Query(
        "SELECT COALESCE(SUM(CASE WHEN tipo = 'INGRESO' THEN cantidad ELSE -cantidad END), 0) " +
                "FROM movimientos WHERE productoId = :productoId"
    )
    suspend fun saldoDe(productoId: String): Int
}
