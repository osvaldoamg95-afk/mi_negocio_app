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
}
