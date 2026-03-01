package com.tunegocio.app.ui

import android.os.Bundle
import android.widget.Toast
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

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        // Toggle de Modos
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    binding.btnModeDay.id -> viewModel.setMode(ReportsViewModel.ReportMode.DAY)
                    binding.btnModeMonth.id -> viewModel.setMode(ReportsViewModel.ReportMode.MONTH)
                    binding.btnModeYear.id -> viewModel.setMode(ReportsViewModel.ReportMode.YEAR)
                }
            }
        }

        // Navegación
        binding.btnPrev.setOnClickListener { viewModel.prevPeriod() }
        binding.btnNext.setOnClickListener { viewModel.nextPeriod() }

        // Exportar (Por ahora visual, luego conectamos ImportHelper)
        binding.btnExport.setOnClickListener {
            Toast.makeText(this, "Exportando reporte actual...", Toast.LENGTH_SHORT).show()
            // Aquí llamaremos a ExportHelper.exportCurrentReport(data)
        }
    }

    private fun setupObservers() {
        viewModel.reportState.observe(this) { state ->
            binding.txtCurrentPeriod.text = state.title
            
            binding.txtSales.text = "$ %.2f".format(state.totalSales)
            binding.txtCost.text = "$ %.2f".format(state.totalCost)
            binding.txtGross.text = "$ %.2f".format(state.grossProfit)
            binding.txtExpenses.text = "$ %.2f".format(state.totalExpenses)
            binding.txtNet.text = "$ %.2f".format(state.netProfit)
            
            // Colorear Utilidad Neta
            val color = if (state.netProfit >= 0) 
                android.graphics.Color.parseColor("#2E7D32") 
            else 
                android.graphics.Color.RED
            binding.txtNet.setTextColor(color)
        }
    }
}
