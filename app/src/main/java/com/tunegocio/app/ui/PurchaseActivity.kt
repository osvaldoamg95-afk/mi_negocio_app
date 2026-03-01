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
import com.tunegocio.app.databinding.ActivityPurchaseBinding
import com.tunegocio.app.viewmodel.PurchaseViewModel
import kotlinx.coroutines.launch

class PurchaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPurchaseBinding
    private val viewModel: PurchaseViewModel by viewModels()
    private lateinit var db: AppDatabase // Solo para cargar productos UI

    private var selectedProductId: Int = -1
    private var selectedProductName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        loadProducts()
        setupObservers()

        binding.btnAddPurchase.setOnClickListener {
            val qty = binding.etQuantity.text.toString().toDoubleOrNull() ?: 0.0
            val price = binding.etPurchasePrice.text.toString().toDoubleOrNull() ?: 0.0

            if (selectedProductId != -1 && qty > 0) {
                viewModel.addToCart(selectedProductId, selectedProductName, qty, price)
                binding.etQuantity.setText("")
                binding.etPurchasePrice.setText("")
            }
        }

        binding.btnSavePurchase.setOnClickListener {
            viewModel.savePurchase()
        }
    }

    private fun setupObservers() {
        viewModel.cart.observe(this) { items ->
            if (items.isEmpty()) {
                binding.txtPurchaseCart.text = "Carrito vacío"
            } else {
                val sb = StringBuilder("📦 COMPRAS:\n\n")
                items.forEachIndexed { i, item ->
                    sb.append("${i + 1}. ${item.productName} x ${item.quantity} ($${item.price})\n")
                }
                binding.txtPurchaseCart.text = sb.toString()
            }
        }

        viewModel.statusMessage.observe(this) { msg ->
            binding.txtStatus.text = msg
            if (msg.startsWith("❌")) Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            val products = db.productDao().getAllList()
            if (products.isNotEmpty()) {
                val adapter = ArrayAdapter(
                    this@PurchaseActivity,
                    android.R.layout.simple_spinner_item,
                    products.map { it.name }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spProducts.adapter = adapter

                binding.spProducts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val p = products[position]
                        selectedProductId = p.id
                        selectedProductName = p.name
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }
    }
}
