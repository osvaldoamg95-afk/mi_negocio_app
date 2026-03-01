package com.tunegocio.app.ui

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

class SalesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalesBinding
    private val viewModel: SalesViewModel by viewModels() // ✅ ViewModel inyectado
    private lateinit var db: AppDatabase // Solo para cargar productos en UI

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

    private fun setupObservers() {
        // Observar cambios en el carrito
        viewModel.cart.observe(this) { items ->
            if (items.isEmpty()) {
                binding.txtCart.text = "Carrito vacío"
            } else {
                val sb = StringBuilder("🛒 CARRITO:\n\n")
                items.forEachIndexed { i, item ->
                    sb.append("${i + 1}. ${item.productName} x ${item.quantity}\n")
                }
                binding.txtCart.text = sb.toString()
            }
        }

        // Observar mensajes de estado
        viewModel.statusMessage.observe(this) { msg ->
            binding.txtStatus.text = msg
            // Opcional: Mostrar Toast si es error grave
            if (msg.startsWith("❌")) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
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
