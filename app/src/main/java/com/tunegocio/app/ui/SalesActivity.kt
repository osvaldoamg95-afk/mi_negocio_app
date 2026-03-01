package com.tunegocio.app.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.databinding.ActivitySalesBinding
import com.tunegocio.app.viewmodel.SalesViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

class SalesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalesBinding
    private val viewModel: SalesViewModel by viewModels()
    private lateinit var db: AppDatabase

    private var selectedProductId: Int = -1
    private var selectedProductPrice: Double = 0.0
    private var selectedProductName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        loadProducts()
        setupObservers()
        updateDateButton() // Mostrar fecha inicial

        // ✅ Selector de Fecha
        binding.btnDate.setOnClickListener {
            val c = viewModel.saleDate
            val dpd = DatePickerDialog(this, { _, year, month, day ->
                viewModel.setDate(year, month, day)
                updateDateButton()
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
            dpd.show()
        }

        binding.btnAddToCart.setOnClickListener {
            val qty = binding.etQuantity.text.toString().toDoubleOrNull() ?: 0.0
            if (selectedProductId != -1 && qty > 0) {
                viewModel.addToCart(selectedProductId, selectedProductName, qty, selectedProductPrice)
                binding.etQuantity.setText("")
            }
        }

        binding.btnClearCart.setOnClickListener {
            viewModel.clearCart()
        }

        binding.btnSell.setOnClickListener {
            viewModel.processSale()
        }
    }

    private fun updateDateButton() {
        binding.btnDate.text = "📅 ${viewModel.getDateString()}"
    }

    private fun setupObservers() {
        viewModel.cart.observe(this) { items ->
            if (items.isEmpty()) {
                binding.txtCart.text = "El carrito está vacío."
            } else {
                val sb = StringBuilder()
                items.forEachIndexed { i, item ->
                    val sub = item.quantity * item.price
                    sb.append("${i + 1}. ${item.productName}\n   ${item.quantity} x $${item.price} = $${String.format("%.2f", sub)}\n\n")
                }
                binding.txtCart.text = sb.toString()
            }
        }

        viewModel.totalAmount.observe(this) { total ->
            binding.txtTotal.text = "$ %.2f".format(total)
        }

        viewModel.statusMessage.observe(this) { msg ->
            binding.txtStatus.text = msg
            if (msg.startsWith("❌") || msg.startsWith("✅")) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
            // Si la venta fue exitosa, resetear fecha visualmente
            if (msg.startsWith("✅")) updateDateButton()
        }
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            val products = db.productDao().getAllList()
            if (products.isNotEmpty()) {
                val adapter = ArrayAdapter(
                    this@SalesActivity,
                    android.R.layout.simple_spinner_item,
                    products.map { it.name }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spProducts.adapter = adapter

                binding.spProducts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val p = products[position]
                        selectedProductId = p.id
                        selectedProductPrice = p.salePrice
                        selectedProductName = p.name
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }
    }
}
