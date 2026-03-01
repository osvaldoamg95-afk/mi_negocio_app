package com.tunegocio.app.utils

import android.content.Context
import android.os.Environment
import com.tunegocio.app.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object ExportHelper {

    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    suspend fun exportSales(context: Context, db: AppDatabase): String {
        return withContext(Dispatchers.IO) {
            val file = createFile("REPORTE_VENTAS.csv")
            val writer = FileWriter(file)
            writer.append("FECHA,TOTAL,COSTO,GANANCIA\n")

            val sales = db.saleDao().getAllSales()
            for (sale in sales) {
                writer.append(
                    "${sdf.format(Date(sale.date))}," +
                    "${sale.total}," +
                    "${sale.costTotal}," +
                    "${sale.profit}\n"
                )
            }
            writer.flush()
            writer.close()
            file.absolutePath
        }
    }

    suspend fun exportExpenses(context: Context, db: AppDatabase): String {
        return withContext(Dispatchers.IO) {
            val file = createFile("REPORTE_GASTOS.csv")
            val writer = FileWriter(file)
            writer.append("FECHA,DESCRIPCION,MONTO,CATEGORIA\n")

            val expenses = db.expenseDao().getAllExpenses()
            for (e in expenses) {
                writer.append(
                    "${sdf.format(Date(e.date))}," +
                    "${e.description.uppercase()}," +
                    "${e.amount}," +
                    "${e.category.uppercase()}\n"
                )
            }
            writer.flush()
            writer.close()
            file.absolutePath
        }
    }

    // ✅ REPORTE DE INVENTARIO FÍSICO DETALLADO
    suspend fun exportInventory(context: Context, db: AppDatabase): String {
        return withContext(Dispatchers.IO) {
            val file = createFile("INVENTARIO_FISICO.csv")
            val writer = FileWriter(file)
            
            // Encabezados Profesionales
            writer.append("ID,PRODUCTO,TIPO,COSTO_PROM,PRECIO_VENTA,STOCK_SISTEMA,STOCK_FISICO,DIFERENCIA\n")

            val products = db.productDao().getAllList()

            for (p in products) {
                val stock = db.inventoryLotDao().getTotalStock(p.id) ?: 0.0
                
                // Calculamos costo promedio ponderado si hay stock
                val lots = db.inventoryLotDao().getLotsForProduct(p.id)
                var totalCost = 0.0
                var totalQty = 0.0
                for (l in lots) {
                    if (l.quantity > 0) {
                        totalCost += l.quantity * l.purchasePrice
                        totalQty += l.quantity
                    }
                }
                val avgCost = if (totalQty > 0) totalCost / totalQty else 0.0
                val type = if (p.isManufactured) "MANUF" else "INSUMO"

                writer.append(
                    "${p.id}," +
                    "${p.name.uppercase()}," +
                    "$type," +
                    "%.2f,".format(avgCost) +
                    "${p.salePrice}," +
                    "$stock," +
                    "," + // Espacio vacío para conteo físico
                    "\n"  // Espacio para anotar diferencia
                )
            }

            writer.flush()
            writer.close()
            file.absolutePath
        }
    }

    private fun createFile(name: String): File {
        val dir = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!dir.exists()) dir.mkdirs()
        return File(dir, name)
    }
}
