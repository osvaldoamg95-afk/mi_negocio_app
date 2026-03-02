package com.tunegocio.app

import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.databinding.ActivityMainBinding
import com.tunegocio.app.ui.*
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        setupButtons()
        updateDashboard()
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
    }

    private fun setupButtons() {
        binding.btnVentas.setOnClickListener {
            startActivity(Intent(this, SalesActivity::class.java))
        }

        binding.btnInventario.setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }

        binding.btnOpenPurchase.setOnClickListener {
            startActivity(Intent(this, PurchaseActivity::class.java))
        }

        binding.btnGastos.setOnClickListener {
            startActivity(Intent(this, ExpenseActivity::class.java))
        }

        binding.btnReportes.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        binding.btnCloseDay.setOnClickListener {
            startActivity(Intent(this, CloseDayActivity::class.java))
        }

        // ✅ Eliminamos botones viejos de Recetas y Materias Primas
        // (Ahora se gestionan dentro de Inventario)

        binding.btnTools.setOnClickListener {
             startActivity(Intent(this, BackupActivity::class.java))
        }
    }

    private fun updateDashboard() {
        lifecycleScope.launch {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis

            val salesToday = db.saleDao().getTodaySales(startOfDay) ?: 0.0
            val profitToday = db.saleDao().getTodayProfit(startOfDay) ?: 0.0
            val lowStockCount = db.inventoryLotDao().countLowStockProducts() ?: 0

            binding.txtTodaySales.text = "$ %.2f".format(salesToday)
            binding.txtTodayProfit.text = "$ %.2f".format(profitToday)

            if (lowStockCount > 0) {
                binding.txtAlertStock.text = "⚠️ Atención: $lowStockCount productos con stock bajo"
                binding.txtAlertStock.setTextColor(Color.RED)
            } else {
                binding.txtAlertStock.text = "✅ Inventario saludable"
                binding.txtAlertStock.setTextColor(Color.parseColor("#2E7D32"))
            }
        }
    }
}
