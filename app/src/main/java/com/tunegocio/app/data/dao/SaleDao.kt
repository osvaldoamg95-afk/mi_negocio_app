package com.tunegocio.app.data.dao

import androidx.room.*
import com.tunegocio.app.data.entities.Sale
import com.tunegocio.app.data.entities.SaleDetail

@Dao
interface SaleDao {

    @Insert
    suspend fun insertSale(sale: Sale): Long

    @Insert
    suspend fun insertSaleDetail(detail: SaleDetail)

    @Query("SELECT * FROM sales ORDER BY date DESC")
    suspend fun getAllSales(): List<Sale>

    @Query("""
    SELECT SUM(total) FROM sales
    WHERE date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalSalesBetween(startDate: Long, endDate: Long): Double?

    @Query("""
    SELECT SUM(costTotal) FROM sales
    WHERE date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalCostBetween(startDate: Long, endDate: Long): Double?

    @Query("""
    SELECT SUM(profit) FROM sales
    WHERE date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalProfitBetween(startDate: Long, endDate: Long): Double?
}
