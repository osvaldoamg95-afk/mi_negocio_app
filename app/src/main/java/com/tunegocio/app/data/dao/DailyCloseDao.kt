package com.tunegocio.app.data.dao

import androidx.room.*
import com.tunegocio.app.data.entities.DailyClose

@Dao
interface DailyCloseDao {

    @Insert
    suspend fun insert(close: DailyClose)

    @Query("SELECT * FROM daily_close ORDER BY date DESC")
    suspend fun getAll(): List<DailyClose>

    @Query("""
        SELECT COUNT(*) FROM daily_close
        WHERE date BETWEEN :start AND :end
    """)
    suspend fun isClosed(start: Long, end: Long): Int
}
