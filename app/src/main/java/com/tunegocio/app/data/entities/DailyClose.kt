package com.tunegocio.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_close")
data class DailyClose(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long,
    val totalSales: Double,
    val totalExpenses: Double,
    val netProfit: Double
)
