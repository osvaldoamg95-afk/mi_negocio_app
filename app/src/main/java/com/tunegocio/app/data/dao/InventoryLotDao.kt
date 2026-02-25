package com.tunegocio.app.data.dao

import androidx.room.*
import com.tunegocio.app.data.entities.InventoryLot

@Dao
interface InventoryLotDao {

    @Insert
    suspend fun insert(lot: InventoryLot)

    // Obtener todos los lotes de un producto
    @Query("SELECT * FROM inventory_lots WHERE productId = :productId")
    suspend fun getLotsForProduct(productId: Int): List<InventoryLot>

    // Obtener stock total
    @Query("SELECT SUM(quantity) FROM inventory_lots WHERE productId = :productId")
    suspend fun getTotalStock(productId: Int): Double?

    // ✅ Obtener lotes ordenados por fecha (FIFO)
    @Query("""
        SELECT * FROM inventory_lots
        WHERE productId = :productId AND quantity > 0
        ORDER BY purchaseDate ASC
    """)
    suspend fun getLotsFIFO(productId: Int): List<InventoryLot>

    // ✅ Actualizar lote (cuando se descuenta)
    @Update
    suspend fun updateLot(lot: InventoryLot)
}
