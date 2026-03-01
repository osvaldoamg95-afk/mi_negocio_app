package com.tunegocio.app.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object BackupHelper {

    fun backupDatabase(context: Context): String {

        val dbFile = context.getDatabasePath("mi_negocio_db")

        val backupDir = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }

        val backupFile = File(backupDir, "mi_negocio_backup.db")

        FileInputStream(dbFile).use { input ->
            FileOutputStream(backupFile).use { output ->
                input.copyTo(output)
            }
        }

        return backupFile.absolutePath
    }

    fun restoreDatabase(context: Context): String {

        val dbFile = context.getDatabasePath("mi_negocio_db")

        val backupDir = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        val backupFile = File(backupDir, "mi_negocio_backup.db")

        FileInputStream(backupFile).use { input ->
            FileOutputStream(dbFile).use { output ->
                input.copyTo(output)
            }
        }

        return "Base restaurada correctamente"
    }
}
