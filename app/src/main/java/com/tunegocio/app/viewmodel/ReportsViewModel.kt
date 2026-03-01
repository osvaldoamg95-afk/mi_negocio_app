package com.tunegocio.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tunegocio.app.data.AppDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    // Estado del Reporte
    data class ReportState(
        val title: String,
        val rangeLabel: String,
        val totalSales: Double,
        val totalCost: Double,
        val grossProfit: Double,
        val totalExpenses: Double,
        val totalMermas: Double, // ✅ Nuevo: Mermas (Si implementamos tabla merma, por ahora simular o calcular)
        val netProfit: Double
    )

    private val _reportState = MutableLiveData<ReportState>()
    val reportState: LiveData<ReportState> = _reportState

    // Control de Fechas
    private var currentDate = Calendar.getInstance()
    private var currentMode = ReportMode.MONTH // Por defecto Mes

    enum class ReportMode { DAY, MONTH, YEAR }

    init {
        updateReport()
    }

    fun setMode(mode: ReportMode) {
        currentMode = mode
        currentDate = Calendar.getInstance() // Reset a hoy al cambiar modo
        updateReport()
    }

    fun prevPeriod() {
        when (currentMode) {
            ReportMode.DAY -> currentDate.add(Calendar.DAY_OF_YEAR, -1)
            ReportMode.MONTH -> currentDate.add(Calendar.MONTH, -1)
            ReportMode.YEAR -> currentDate.add(Calendar.YEAR, -1)
        }
        updateReport()
    }

    fun nextPeriod() {
        when (currentMode) {
            ReportMode.DAY -> currentDate.add(Calendar.DAY_OF_YEAR, 1)
            ReportMode.MONTH -> currentDate.add(Calendar.MONTH, 1)
            ReportMode.YEAR -> currentDate.add(Calendar.YEAR, 1)
        }
        updateReport()
    }

    private fun updateReport() {
        val (start, end) = calculateRange()
        val title = getTitle()

        viewModelScope.launch {
            val sales = db.saleDao().getTotalSalesBetween(start, end) ?: 0.0
            val cost = db.saleDao().getTotalCostBetween(start, end) ?: 0.0
            val profit = db.saleDao().getTotalProfitBetween(start, end) ?: 0.0
            val expenses = db.expenseDao().getTotalExpensesBetween(start, end) ?: 0.0
            
            // Si tuviéramos tabla mermas, aquí la consultamos.
            // Por ahora asumimos que la merma se registró como Gasto o reducción de stock sin venta.
            // Si la registraste como Gasto con categoría "MERMA", ya está sumada en expenses.
            val mermas = 0.0 

            val net = profit - expenses - mermas

            _reportState.value = ReportState(
                title = title,
                rangeLabel = formatDateRange(start, end),
                totalSales = sales,
                totalCost = cost,
                grossProfit = profit,
                totalExpenses = expenses,
                totalMermas = mermas,
                netProfit = net
            )
        }
    }

    private fun calculateRange(): Pair<Long, Long> {
        val start = currentDate.clone() as Calendar
        val end = currentDate.clone() as Calendar

        when (currentMode) {
            ReportMode.DAY -> {
                start.set(Calendar.HOUR_OF_DAY, 0)
                start.set(Calendar.MINUTE, 0)
                start.set(Calendar.SECOND, 0)
                start.set(Calendar.MILLISECOND, 0)
                
                end.set(Calendar.HOUR_OF_DAY, 23)
                end.set(Calendar.MINUTE, 59)
                end.set(Calendar.SECOND, 59)
            }
            ReportMode.MONTH -> {
                start.set(Calendar.DAY_OF_MONTH, 1)
                start.set(Calendar.HOUR_OF_DAY, 0)
                start.set(Calendar.MINUTE, 0)
                
                end.set(Calendar.DAY_OF_MONTH, start.getActualMaximum(Calendar.DAY_OF_MONTH))
                end.set(Calendar.HOUR_OF_DAY, 23)
                end.set(Calendar.MINUTE, 59)
            }
            ReportMode.YEAR -> {
                start.set(Calendar.DAY_OF_YEAR, 1)
                start.set(Calendar.HOUR_OF_DAY, 0)
                
                end.set(Calendar.MONTH, 11) // Diciembre
                end.set(Calendar.DAY_OF_MONTH, 31)
                end.set(Calendar.HOUR_OF_DAY, 23)
            }
        }
        return Pair(start.timeInMillis, end.timeInMillis)
    }

    private fun getTitle(): String {
        val fmt = when (currentMode) {
            ReportMode.DAY -> SimpleDateFormat("dd 'de' MMMM yyyy", Locale.getDefault())
            ReportMode.MONTH -> SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            ReportMode.YEAR -> SimpleDateFormat("yyyy", Locale.getDefault())
        }
        return fmt.format(currentDate.time).uppercase()
    }
    
    private fun formatDateRange(start: Long, end: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        return "${sdf.format(start)} - ${sdf.format(end)}"
    }
}
