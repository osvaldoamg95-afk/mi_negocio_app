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
import androidx.recyclerview.widget.LinearLayoutManager
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.databinding.ActivitySalesBinding
import com.tunegocio.app.ui.adapters.CartAdapter
import com.tunegocio.app.viewmodel.SalesViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

class SalesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalesBinding
    private val viewModel: SalesViewModel by viewModels()
    private lateinit var db: AppDatabase
    private lateinit var cartAdapter: CartAdapter // ✅ Adaptador

    private var selectedProductId: Int = -1
    private var selectedProductPrice: Double = 0.0
    private var selectedProductName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        setupRecyclerView() // ✅ Configurar lista
        loadProducts()
        setupObservers()
        updateDateButton()

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

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onDeleteClick = { item ->
                viewModel.removeFromCart(item) // ✅ Acción eliminar
            }
        )
        binding.rvCart.layoutManager = LinearLayoutManager(this)
        binding.rvCart.adapter = cartAdapter
    }

    private fun updateDateButton() {
        binding.btnDate.text = "📅 ${viewModel.getDateString()}"
    }

    private fun setupObservers() {
        viewModel.cart.observe(this) { items ->
            cartAdapter.submitList(items.toList()) // ✅ Actualizar lista visual
        }

        viewModel.totalAmount.observe(this) { total ->
            binding.txtTotal.text = "$ %.2f".format(total)
        }

        viewModel.statusMessage.observe(this) { msg ->
            binding.txtStatus.text = msg
            if (msg.startsWith("❌") || msg.startsWith("✅")) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
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
