package com.tunegocio.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import com.tunegocio.app.data.entities.*
import com.tunegocio.app.data.dao.*
import com.tunegocio.app.data.utils.Converters // Crearemos esto

@Database(
    entities = [
        Product::class,
        ProductIngredient::class, // ✅ Nueva
        InventoryLot::class,
        Sale::class,
        SaleDetail::class,
        Expense::class,
        DailyClose::class
    ],
    version = 8, // 🔥 Subimos versión por cambio estructural masivo
    exportSchema = false
)
@TypeConverters(Converters::class) // ✅ Para el Enum ProductType
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun inventoryLotDao(): InventoryLotDao
    abstract fun saleDao(): SaleDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun dailyCloseDao(): DailyCloseDao

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
                .fallbackToDestructiveMigration() // Borrón y cuenta nueva necesario
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
