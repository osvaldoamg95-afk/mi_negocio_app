package com.tunegocio.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.databinding.ActivityExportBinding
import com.tunegocio.app.utils.ExportHelper

class ExportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExportBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        binding.btnExportSales.setOnClickListener {
            exportSales()
        }

        binding.btnExportExpenses.setOnClickListener {
            exportExpenses()
        }

        binding.btnExportInventory.setOnClickListener {
            exportInventory()
        }
    }

    private fun exportSales() {
        lifecycleScope.launch {
            val path = ExportHelper.exportSales(this@ExportActivity, db)
            binding.txtStatus.text = "Ventas exportadas en:\n$path"
        }
    }

    private fun exportExpenses() {
        lifecycleScope.launch {
            val path = ExportHelper.exportExpenses(this@ExportActivity, db)
            binding.txtStatus.text = "Gastos exportados en:\n$path"
        }
    }

    private fun exportInventory() {
        lifecycleScope.launch {
            val path = ExportHelper.exportInventory(this@ExportActivity, db)
            binding.txtStatus.text = "Inventario exportado en:\n$path"
        }
    }
}
