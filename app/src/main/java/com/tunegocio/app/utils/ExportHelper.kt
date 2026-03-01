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

            val file = createFile("ventas.csv")

            val writer = FileWriter(file)

            writer.append("Fecha,Total,Costo,Ganancia\n")

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

            val file = createFile("gastos.csv")
            val writer = FileWriter(file)

            writer.append("Fecha,Descripcion,Monto,Categoria\n")

            val expenses = db.expenseDao().getAllExpenses()

            for (e in expenses) {
                writer.append(
                    "${sdf.format(Date(e.date))}," +
                    "${e.description}," +
                    "${e.amount}," +
                    "${e.category}\n"
                )
            }

            writer.flush()
            writer.close()

            file.absolutePath
        }
    }

    suspend fun exportInventory(context: Context, db: AppDatabase): String {
        return withContext(Dispatchers.IO) {

            val file = createFile("inventario.csv")
            val writer = FileWriter(file)

            writer.append("ProductoID,Cantidad,PrecioCompra\n")

            val lots = db.inventoryLotDao().getAllLots()

            for (lot in lots) {
                writer.append(
                    "${lot.productId}," +
                    "${lot.quantity}," +
                    "${lot.purchasePrice}\n"
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
