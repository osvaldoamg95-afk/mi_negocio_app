package com.tunegocio.app.utils

import android.content.Context
import android.os.Environment
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.data.entities.InventoryLot
import com.tunegocio.app.data.entities.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object ImportHelper {

    private const val FILE_NAME = "PLANTILLA_IMPORTAR.csv"

    // ✅ PASO 1: Generar Plantilla para que el usuario rellene
    suspend fun generateTemplate(): String {
        return withContext(Dispatchers.IO) {
            val file = createFile(FILE_NAME)
            val writer = FileWriter(file)

            // Encabezados claros
            writer.append("NOMBRE_PRODUCTO,PRECIO_VENTA,ES_MANUFACTURADO(SI/NO),STOCK_INICIAL,COSTO_UNITARIO\n")
            
            // Ejemplos
            writer.append("COCA COLA 600ML,1.50,NO,24,0.80\n")
            writer.append("HAMBURGUESA ESPECIAL,5.00,SI,0,0\n")
            writer.append("PAN DE HAMBURGUESA,0.00,NO,100,0.20\n")
            
            writer.flush()
            writer.close()
            
            file.absolutePath
        }
    }

    // ✅ PASO 2: Importar Productos e Inventario
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

            reader.readLine() // Saltar encabezado

            while (reader.readLine().also { line = it } != null) {
                try {
                    val parts = line!!.split(",")
                    if (parts.size >= 5) {
                        val name = parts[0].uppercase().trim()
                        val price = parts[1].toDoubleOrNull() ?: 0.0
                        val isManuf = parts[2].trim().equals("SI", ignoreCase = true)
                        val stock = parts[3].toDoubleOrNull() ?: 0.0
                        val cost = parts[4].toDoubleOrNull() ?: 0.0

                        if (name.isNotEmpty()) {
                            // 1. Crear Producto
                            db.productDao().insert(
                                Product(name = name, salePrice = price, isManufactured = isManuf)
                            )

                            // 2. Si hay stock inicial, crear lote automáticamente
                            if (stock > 0) {
                                // Necesitamos el ID del producto recién creado.
                                // Como Room es rápido, buscamos por nombre (ya que acabamos de insertar)
                                // OJO: Idealmente el DAO insert debería devolver Long (ID), 
                                // pero para no cambiar todo ahora, buscamos.
                                val product = db.productDao().getByName(name) // Necesitamos agregar esto al DAO
                                
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
            "✅ Importación completa: $count productos cargados. ($errors errores)"
        }
    }

    private fun createFile(name: String): File {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!dir.exists()) dir.mkdirs()
        return File(dir, name)
    }
}
