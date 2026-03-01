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
        SELECT * FROM sale_details
        WHERE saleId = :saleId
    """)
    suspend fun getDetailsForSale(saleId: Int): List<SaleDetail>

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

    @Query("SELECT SUM(profit) FROM sales")
    suspend fun getTotalAccumulatedProfit(): Double?

    @Query("""
    SELECT SUM(total) FROM sales 
    WHERE date >= :startOfDay
    """)
    suspend fun getTodaySales(startOfDay: Long): Double?

    @Query("""
    SELECT SUM(profit) FROM sales 
    WHERE date >= :startOfDay
    """)
    suspend fun getTodayProfit(startOfDay: Long): Double?
}
