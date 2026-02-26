package com.tunegocio.app.data.dao

import androidx.room.*
import com.tunegocio.app.data.entities.Expense

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: Expense)

    @Query("""
        SELECT SUM(amount) FROM expenses
        WHERE date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalExpensesBetween(startDate: Long, endDate: Long): Double?
}
