package com.tunegocio.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.databinding.ActivityReportsBinding
import java.util.Calendar

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        binding.btnMonthly.setOnClickListener {
            generateMonthlyReport()
        }

        binding.btnAnnual.setOnClickListener {
            generateAnnualReport()
        }
    }

    private fun generateMonthlyReport() {
        lifecycleScope.launch {

            val calendar = Calendar.getInstance()

            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val start = calendar.timeInMillis

            calendar.add(Calendar.MONTH, 1)
            val end = calendar.timeInMillis

            showReport(start, end)
        }
    }

    private fun generateAnnualReport() {
        lifecycleScope.launch {

            val calendar = Calendar.getInstance()

            calendar.set(Calendar.DAY_OF_YEAR, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val start = calendar.timeInMillis

            calendar.add(Calendar.YEAR, 1)
            val end = calendar.timeInMillis

            showReport(start, end)
        }
    }

    private suspend fun showReport(start: Long, end: Long) {

        val total = db.saleDao().getTotalSalesBetween(start, end) ?: 0.0
        val cost = db.saleDao().getTotalCostBetween(start, end) ?: 0.0
        val profit = db.saleDao().getTotalProfitBetween(start, end) ?: 0.0
        val expenses = db.expenseDao().getTotalExpensesBetween(start, end) ?: 0.0

        val netProfit = profit - expenses

        binding.txtReport.text = """
            Ventas: %.2f
            Costo: %.2f
            Ganancia Bruta: %.2f
            Gastos: %.2f
            ------------------------
            UTILIDAD NETA: %.2f
        """.trimIndent().format(total, cost, profit, expenses, netProfit)
    }
}
