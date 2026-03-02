package com.tunegocio.app.utils

import android.content.Context
import android.os.Environment
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.data.entities.InventoryLot
import com.tunegocio.app.data.entities.Product
import com.tunegocio.app.data.entities.ProductType // ✅
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object ImportHelper {

    private const val FILE_NAME = "PLANTILLA_IMPORTAR.csv"

    suspend fun generateTemplate(): String {
        return withContext(Dispatchers.IO) {
            val file = createFile(FILE_NAME)
            val writer = FileWriter(file)

            writer.append("NOMBRE_PRODUCTO,PRECIO_VENTA,TIPO(INSUMO/SIMPLE/MANUFACTURADO),STOCK_INICIAL,COSTO_UNITARIO\n")
            writer.append("COCA COLA 600ML,1.50,SIMPLE,24,0.80\n")
            writer.append("HAMBURGUESA ESPECIAL,5.00,MANUFACTURADO,0,0\n")
            writer.append("PAN DE HAMBURGUESA,0.00,INSUMO,100,0.20\n")
            
            writer.flush()
            writer.close()
            file.absolutePath
        }
    }

    suspend fun importData(context: Context, db: AppDatabase): String {
        return withContext(Dispatchers.IO) {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                FILE_NAME
            )

            if (!file.exists()) return@withContext "❌ Archivo no encontrado. Primero genera la plantilla."

            val reader = BufferedReader(FileReader(file))
            var line: String?
            var count = 0
            var errors = 0

            reader.readLine() 

            while (reader.readLine().also { line = it } != null) {
                try {
                    val parts = line!!.split(",")
                    if (parts.size >= 5) {
                        val name = parts[0].uppercase().trim()
                        val price = parts[1].toDoubleOrNull() ?: 0.0
                        
                        // ✅ Convertir String a Enum
                        val typeStr = parts[2].trim().uppercase()
                        val type = when {
                            typeStr.contains("MANUF") -> ProductType.MANUFACTURADO
                            typeStr.contains("INSUMO") -> ProductType.INSUMO
                            else -> ProductType.PRODUCTO_SIMPLE
                        }

                        val stock = parts[3].toDoubleOrNull() ?: 0.0
                        val cost = parts[4].toDoubleOrNull() ?: 0.0

                        if (name.isNotEmpty()) {
                            db.productDao().insert(
                                Product(name = name, salePrice = price, type = type) // ✅
                            )

                            if (stock > 0 && type != ProductType.MANUFACTURADO) {
                                val product = db.productDao().getByName(name)
                                if (product != null) {
                                    db.inventoryLotDao().insert(
                                        InventoryLot(
                                            productId = product.id,
                                            quantity = stock,
                                            purchasePrice = cost,
                                            purchaseDate = System.currentTimeMillis()
                                        )
                                    )
                                }
                            }
                            count++
                        }
                    }
                } catch (e: Exception) {
                    errors++
                }
            }
            reader.close()
            "✅ Importación completa: $count cargados. ($errors errores)"
        }
    }

    private fun createFile(name: String): File {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!dir.exists()) dir.mkdirs()
        return File(dir, name)
    }
}
