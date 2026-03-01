package com.tunegocio.app.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tunegocio.app.databinding.ActivityInventoryBinding
import com.tunegocio.app.ui.adapters.ProductAdapter
import com.tunegocio.app.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private val viewModel: InventoryViewModel by viewModels()
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etProductName.filters = arrayOf(InputFilter.AllCaps())

        setupRecyclerView()
        setupObservers()

        binding.btnSaveProduct.setOnClickListener {
            val name = binding.etProductName.text.toString()
            val price = binding.etProductPrice.text.toString().toDoubleOrNull() ?: 0.0
            val isManufactured = binding.chkManufactured.isChecked
            
            viewModel.saveProduct(name, price, isManufactured)
            clearFields()
        }
        
        binding.btnCancelEdit.setOnClickListener {
            viewModel.cancelEdit()
            clearFields()
        }

        binding.btnOpenPurchase.setOnClickListener {
            startActivity(Intent(this, PurchaseActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        // El adaptador necesita una función síncrona para pintar el stock rápido
        // Usaremos runBlocking por simplicidad en UI pequeña, o idealmente cache en ViewModel.
        // Para esta fase, pediremos al ViewModel el dato.
        adapter = ProductAdapter(
            onEditClick = { product ->
                viewModel.selectProductForEdit(product)
                binding.etProductName.setText(product.name)
                binding.etProductPrice.setText(product.salePrice.toString())
                binding.chkManufactured.isChecked = product.isManufactured
                binding.btnSaveProduct.text = "ACTUALIZAR PRODUCTO"
                binding.btnCancelEdit.visibility = View.VISIBLE
            },
            stockProvider = { id -> 
                // Truco rápido: En un entorno real esto iría en el objeto Product con un JOIN
                // Pero aquí usamos runBlocking seguro porque es DB local rapidísima.
                var stock = 0.0
                runBlocking { stock = viewModel.getStockForProduct(id) }
                stock
            }
        )
        
        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = adapter
    }

    private fun clearFields() {
        binding.etProductName.setText("")
        binding.etProductPrice.setText("")
        binding.chkManufactured.isChecked = false
        binding.btnSaveProduct.text = "GUARDAR PRODUCTO"
        binding.btnCancelEdit.visibility = View.GONE
    }

    private fun setupObservers() {
        viewModel.statusMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch {
            viewModel.allProducts.collect { products ->
                adapter.submitList(products)
            }
        }
    }
}
