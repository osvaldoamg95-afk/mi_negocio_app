package com.tunegocio.app.utils

import android.content.Context
import android.os.Environment
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.data.entities.Product
import java.io.File
import java.io.BufferedReader
import java.io.FileReader

object ImportHelper {

    suspend fun importProducts(context: Context, db: AppDatabase): String {

        val file = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ),
            "productos_importar.csv"
        )

        if (!file.exists()) return "Archivo no encontrado"

        val reader = BufferedReader(FileReader(file))

        var line: String?

        reader.readLine() // Saltar encabezado

        while (reader.readLine().also { line = it } != null) {

            val parts = line!!.split(",")

            if (parts.size >= 2) {

                val name = parts[0]
                val price = parts[1].toDoubleOrNull() ?: 0.0

                db.productDao().insert(
                    Product(
                        name = name,
                        salePrice = price,
                        isManufactured = false
                    )
                )
            }
        }

        reader.close()

        return "Productos importados correctamente"
    }
}
