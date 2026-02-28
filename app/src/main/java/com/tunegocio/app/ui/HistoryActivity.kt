package com.tunegocio.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.databinding.ActivityHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        binding.btnSalesHistory.setOnClickListener {
            loadSalesHistory()
        }

        binding.btnExpenseHistory.setOnClickListener {
            loadExpenseHistory()
        }
    }

    private fun loadSalesHistory() {

        lifecycleScope.launch {

            val sales = db.saleDao().getAllSales()
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            var text = "📊 HISTORIAL DE VENTAS\n\n"

            for (sale in sales) {

                val date = sdf.format(Date(sale.date))

                text += "Fecha: $date\n"
                text += "Total: ${sale.total}\n"
                text += "Ganancia: ${sale.profit}\n"

                val details = db.saleDao()
                    .getDetailsForSale(sale.id)

                for (d in details) {
                    text += "   - Producto ID ${d.productId} x ${d.quantity}\n"
                }

                text += "------------------------\n"
            }

            binding.txtHistory.text = text
        }
    }

    private fun loadExpenseHistory() {

        lifecycleScope.launch {

            val expenses = db.expenseDao().getAllExpenses()
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            var text = "💰 HISTORIAL DE GASTOS\n\n"

            for (e in expenses) {

                val date = sdf.format(Date(e.date))

                text += "Fecha: $date\n"
                text += "Descripción: ${e.description}\n"
                text += "Monto: ${e.amount}\n"
                text += "Categoría: ${e.category}\n"
                text += "------------------------\n"
            }

            binding.txtHistory.text = text
        }
    }
}
