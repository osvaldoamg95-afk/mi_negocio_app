package com.tunegocio.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.data.entities.DailyClose
import com.tunegocio.app.databinding.ActivityCloseDayBinding
import java.util.*

class CloseDayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCloseDayBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCloseDayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        binding.btnCloseDay.setOnClickListener {

            lifecycleScope.launch {

                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val start = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val end = calendar.timeInMillis

                val total = db.saleDao().getTotalSalesBetween(start, end) ?: 0.0
                val expenses = db.expenseDao().getTotalExpensesBetween(start, end) ?: 0.0
                val profit = db.saleDao().getTotalProfitBetween(start, end) ?: 0.0

                val net = profit - expenses

                db.dailyCloseDao().insert(
                    DailyClose(
                        date = start,
                        totalSales = total,
                        totalExpenses = expenses,
                        netProfit = net
                    )
                )

                binding.txtStatus.text = "✅ Día cerrado correctamente"
            }
        }
    }
}
