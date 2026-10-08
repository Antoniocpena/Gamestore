package com.example.gamestore.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderLineDao {
    @Query("SELECT * FROM order_lines")
    fun observeOrderLines(): Flow<List<OrderLineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLine(line: OrderLineEntity)

    @Delete
    suspend fun deleteLine(line: OrderLineEntity)

    @Query("DELETE FROM order_lines")
    suspend fun clearAll()
}
