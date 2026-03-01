package com.tunegocio.app.data.dao

import androidx.room.*
import com.tunegocio.app.data.entities.InventoryLot

@Dao
interface InventoryLotDao {

    @Insert
    suspend fun insert(lot: InventoryLot)

    @Update
    suspend fun updateLot(lot: InventoryLot)

    @Query("SELECT * FROM inventory_lots WHERE productId = :productId")
    suspend fun getLotsForProduct(productId: Int): List<InventoryLot>

    @Query("SELECT SUM(quantity) FROM inventory_lots WHERE productId = :productId")
    suspend fun getTotalStock(productId: Int): Double?

    // ✅ FIFO PROFESIONAL: Solo trae lotes con stock, ordenados por fecha antigua
    @Query("""
        SELECT * FROM inventory_lots 
        WHERE productId = :productId AND quantity > 0.001 
        ORDER BY purchaseDate ASC
    """)
    suspend fun getLotsFIFO(productId: Int): List<InventoryLot>

    @Query("SELECT SUM(quantity * purchasePrice) FROM inventory_lots")
    suspend fun getInventoryValue(): Double?

    @Query("SELECT * FROM inventory_lots")
    suspend fun getAllLots(): List<InventoryLot>

    // ✅ Para el Dashboard: Contar productos con stock bajo (< 5 unidades)
    @Query("""
        SELECT COUNT(*) FROM (
            SELECT SUM(quantity) as total FROM inventory_lots 
            GROUP BY productId 
            HAVING total < 5
        )
    """)
    suspend fun countLowStockProducts(): Int?
}
