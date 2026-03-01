package com.tunegocio.app.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
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

        // ✅ NUEVO: Botón Eliminar
        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("¿ELIMINAR PRODUCTO?")
                .setMessage("Esta acción no se puede deshacer.")
                .setPositiveButton("ELIMINAR") { _, _ ->
                    viewModel.deleteProduct()
                    clearFields()
                }
                .setNegativeButton("CANCELAR", null)
                .show()
        }

        // ✅ NUEVO: Botón Merma
        binding.btnMerma.setOnClickListener {
            showMermaDialog()
        }

        binding.btnOpenPurchase.setOnClickListener {
            startActivity(Intent(this, PurchaseActivity::class.java))
        }
    }

    private fun showMermaDialog() {
        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = "CANTIDAD A DESCARTAR"

        AlertDialog.Builder(this)
            .setTitle("REGISTRAR MERMA")
            .setMessage("Ingrese la cantidad dañada/vencida:")
            .setView(input)
            .setPositiveButton("CONFIRMAR") { _, _ ->
                val qty = input.text.toString().toDoubleOrNull() ?: 0.0
                if (qty > 0) {
                    viewModel.registerMerma(qty, "Daño Manual")
                    clearFields()
                }
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            onEditClick = { product ->
                viewModel.selectProductForEdit(product)
                binding.etProductName.setText(product.name)
                binding.etProductPrice.setText(product.salePrice.toString())
                binding.chkManufactured.isChecked = product.isManufactured
                
                binding.btnSaveProduct.text = "ACTUALIZAR PRODUCTO"
                binding.btnCancelEdit.visibility = View.VISIBLE
                binding.layoutEditActions.visibility = View.VISIBLE // Mostrar botones peligrosos
            },
            stockProvider = { id -> 
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
        binding.layoutEditActions.visibility = View.GONE // Ocultar botones peligrosos
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
