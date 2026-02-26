package com.tunegocio.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.tunegocio.app.data.entities.Product
import com.tunegocio.app.data.entities.InventoryLot
import com.tunegocio.app.data.entities.Sale
import com.tunegocio.app.data.entities.SaleDetail
import com.tunegocio.app.data.entities.Expense

import com.tunegocio.app.data.dao.ProductDao
import com.tunegocio.app.data.dao.InventoryLotDao
import com.tunegocio.app.data.dao.SaleDao
import com.tunegocio.app.data.dao.ExpenseDao

@Database(
    entities = [
        Product::class,
        InventoryLot::class,
        Sale::class,
        SaleDetail::class,
        Expense::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun inventoryLotDao(): InventoryLotDao
    abstract fun saleDao(): SaleDao
    abstract fun expenseDao(): ExpenseDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mi_negocio_db"
                )
                .fallbackToDestructiveMigration()
                .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
