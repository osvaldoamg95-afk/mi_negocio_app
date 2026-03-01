package com.tunegocio.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tunegocio.app.data.AppDatabase
import kotlinx.coroutines.launch
import java.util.Calendar

class ReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    // Datos del Reporte (LiveData para la UI)
    private val _reportData = MutableLiveData<ReportData>()
    val reportData: LiveData<ReportData> = _reportData

    private val _balanceData = MutableLiveData<BalanceData>()
    val balanceData: LiveData<BalanceData> = _balanceData

    data class ReportData(
        val title: String,
        val totalSales: Double,
        val totalCost: Double,
        val grossProfit: Double,
        val totalExpenses: Double,
        val netProfit: Double
    )

    data class BalanceData(
        val inventoryValue: Double,
        val accumulatedProfit: Double,
        val totalExpensesHistorical: Double,
        val netResult: Double
    )

    // Generar reporte por rango de fechas
    fun generateReport(range: DateRange) {
        val (start, end) = calculateDates(range)
        val title = range.label

        viewModelScope.launch {
            val sales = db.saleDao().getTotalSalesBetween(start, end) ?: 0.0
            val cost = db.saleDao().getTotalCostBetween(start, end) ?: 0.0
            val profit = db.saleDao().getTotalProfitBetween(start, end) ?: 0.0
            val expenses = db.expenseDao().getTotalExpensesBetween(start, end) ?: 0.0
            
            val net = profit - expenses

            _reportData.value = ReportData(title, sales, cost, profit, expenses, net)
        }
    }

    // Generar Balance General (Histórico)
    fun generateGeneralBalance() {
        viewModelScope.launch {
            val inventoryVal = db.inventoryLotDao().getInventoryValue() ?: 0.0
            val accProfit = db.saleDao().getTotalAccumulatedProfit() ?: 0.0
            val accExpenses = db.expenseDao().getTotalExpensesBetween(0, System.currentTimeMillis()) ?: 0.0
            
            val net = accProfit - accExpenses

            _balanceData.value = BalanceData(inventoryVal, accProfit, accExpenses, net)
        }
    }

    // Utilidad de fechas
    enum class DateRange(val label: String) {
        TODAY("Hoy"),
        YESTERDAY("Ayer"),
        THIS_MONTH("Este Mes"),
        THIS_YEAR("Este Año"),
        ALL_TIME("Todo el Tiempo")
    }

    private fun calculateDates(range: DateRange): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val end = System.currentTimeMillis() // Hasta ahora
        
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val start = when (range) {
            DateRange.TODAY -> calendar.timeInMillis
            DateRange.YESTERDAY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val s = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, 1) // Reset para end (aprox)
                // Ajuste fino: Ayer empieza a las 00:00 y termina a las 23:59 de ayer
                // Para simplificar, usaremos rango de inicio a fin explícito si fuera necesario
                s
            }
            DateRange.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.timeInMillis
            }
            DateRange.THIS_YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.timeInMillis
            }
            DateRange.ALL_TIME -> 0L
        }
        
        // Ajuste especial para Ayer (fin del día ayer)
        val finalEnd = if (range == DateRange.YESTERDAY) {
            val c = Calendar.getInstance()
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            c.timeInMillis - 1
        } else {
            // Para el resto, hasta el momento actual o fin de mes
            if(range == DateRange.THIS_MONTH) {
                 val c = Calendar.getInstance()
                 c.add(Calendar.MONTH, 1)
                 c.set(Calendar.DAY_OF_MONTH, 1)
                 c.set(Calendar.HOUR_OF_DAY, 0)
                 c.timeInMillis
            } else {
                System.currentTimeMillis()
            }
        }

        return Pair(start, finalEnd)
    }
}
