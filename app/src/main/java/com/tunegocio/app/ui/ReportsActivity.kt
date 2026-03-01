package com.tunegocio.app.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.tunegocio.app.databinding.ActivityReportsBinding
import com.tunegocio.app.viewmodel.ReportsViewModel

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    private val viewModel: ReportsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
        setupObservers()
        
        // Cargar reporte del mes por defecto al entrar
        viewModel.generateReport(ReportsViewModel.DateRange.THIS_MONTH)
    }

    private fun setupButtons() {
        binding.btnToday.setOnClickListener { 
            viewModel.generateReport(ReportsViewModel.DateRange.TODAY) 
        }
        
        binding.btnMonth.setOnClickListener { 
            viewModel.generateReport(ReportsViewModel.DateRange.THIS_MONTH) 
        }
        
        binding.btnYear.setOnClickListener { 
            viewModel.generateReport(ReportsViewModel.DateRange.THIS_YEAR) 
        }

        binding.btnBalance.setOnClickListener {
            viewModel.generateGeneralBalance()
        }
    }

    private fun setupObservers() {
        // Observar Reporte de Rango (Ventas/Utilidad)
        viewModel.reportData.observe(this) { data ->
            binding.txtReportTitle.text = "Reporte: ${data.title}"
            binding.txtReportContent.text = """
                💰 Ventas Totales:   $ %.2f
                📦 Costo Mercancía:  $ %.2f
                --------------------------------
                🟢 Ganancia Bruta:   $ %.2f
                🔴 Gastos Operativos:$ %.2f
                --------------------------------
                💎 UTILIDAD NETA:    $ %.2f
            """.trimIndent().format(
                data.totalSales, data.totalCost, 
                data.grossProfit, data.totalExpenses, data.netProfit
            )
        }

        // Observar Balance General
        viewModel.balanceData.observe(this) { data ->
            binding.txtReportTitle.text = "🏛️ Balance General Histórico"
            binding.txtReportContent.text = """
                📦 Inventario (Valor):  $ %.2f
                📈 Utilidad Acumulada:  $ %.2f
                💸 Gastos Totales:      $ %.2f
                --------------------------------
                🏁 RESULTADO NETO:      $ %.2f
            """.trimIndent().format(
                data.inventoryValue, data.accumulatedProfit, 
                data.totalExpensesHistorical, data.netResult
            )
        }
    }
}
