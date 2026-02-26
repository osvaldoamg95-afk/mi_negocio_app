package com.tunegocio.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.data.entities.Expense
import com.tunegocio.app.databinding.ActivityExpenseBinding

class ExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpenseBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        binding.btnSaveExpense.setOnClickListener {

            val description = binding.etDescription.text.toString()
            val amount = binding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val category = binding.etCategory.text.toString()

            if (description.isNotEmpty() && amount > 0) {

                lifecycleScope.launch {

                    db.expenseDao().insert(
                        Expense(
                            description = description,
                            amount = amount,
                            date = System.currentTimeMillis(),
                            category = category
                        )
                    )

                    binding.txtStatus.text = "✅ Gasto registrado"
                    binding.etDescription.setText("")
                    binding.etAmount.setText("")
                    binding.etCategory.setText("")
                }
            }
        }
    }
}
