package com.tunegocio.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.data.entities.Product
import com.tunegocio.app.databinding.ActivityInventoryBinding

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        // Guardar producto
        binding.btnSaveProduct.setOnClickListener {

            val name = binding.etProductName.text.toString()
            val price = binding.etProductPrice.text.toString().toDoubleOrNull() ?: 0.0

            if (name.isNotEmpty()) {

                lifecycleScope.launch {

                    val product = Product(
                        name = name,
                        salePrice = price
                    )

                    db.productDao().insert(product)

                    binding.etProductName.setText("")
                    binding.etProductPrice.setText("")

                    loadProducts()
                }
            }
        }

        // ✅ BOTÓN NUEVO PARA IR A COMPRAS
        binding.btnOpenPurchase.setOnClickListener {
            startActivity(Intent(this, PurchaseActivity::class.java))
        }

        loadProducts()
    }

    private fun loadProducts() {

        lifecycleScope.launch {

            db.productDao().getAll().collect { products ->

                var text = ""

                for (p in products) {

                    val stock = db.inventoryLotDao().getTotalStock(p.id) ?: 0.0

                    text += "${p.name} | Precio: ${p.salePrice} | Stock: $stock\n"
                }

                binding.txtProductList.text = text
            }
        }
    }
}
