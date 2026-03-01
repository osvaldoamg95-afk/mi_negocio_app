package com.tunegocio.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tunegocio.app.databinding.ActivityInventoryBinding
import com.tunegocio.app.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private val viewModel: InventoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()

        binding.btnSaveProduct.setOnClickListener {
            val name = binding.etProductName.text.toString()
            val price = binding.etProductPrice.text.toString().toDoubleOrNull() ?: 0.0
            val isManufactured = binding.chkManufactured.isChecked
            
            viewModel.createProduct(name, price, isManufactured)
            
            // Limpiar campos
            binding.etProductName.setText("")
            binding.etProductPrice.setText("")
            binding.chkManufactured.isChecked = false
        }

        binding.btnOpenPurchase.setOnClickListener {
            startActivity(Intent(this, PurchaseActivity::class.java))
        }
    }

    private fun setupObservers() {
        viewModel.statusMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Observar la lista de productos en tiempo real
        lifecycleScope.launch {
            viewModel.allProducts.collect { products ->
                val sb = StringBuilder("PRODUCTOS:\n\n")
                for (p in products) {
                    val stock = viewModel.getStockForProduct(p.id) // Consultamos stock
                    val type = if (p.isManufactured) " (Manufacturado)" else ""
                    sb.append("${p.name}$type | Precio: ${p.salePrice} | Stock: $stock\n")
                }
                binding.txtProductList.text = sb.toString()
            }
        }
    }
}
