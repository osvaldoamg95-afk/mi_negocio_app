package com.tunegocio.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.data.entities.InventoryLot
import com.tunegocio.app.databinding.ActivityPurchaseBinding

class PurchaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPurchaseBinding
    private lateinit var db: AppDatabase
    private var selectedProductId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPurchaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        loadProducts()

        binding.btnSavePurchase.setOnClickListener {

            val quantity = binding.etQuantity.text.toString().toDoubleOrNull() ?: 0.0
            val price = binding.etPurchasePrice.text.toString().toDoubleOrNull() ?: 0.0

            if (selectedProductId != -1 && quantity > 0) {

                lifecycleScope.launch {

                    val lot = InventoryLot(
                        productId = selectedProductId,
                        quantity = quantity,
                        purchasePrice = price,
                        purchaseDate = System.currentTimeMillis()
                    )

                    db.inventoryLotDao().insert(lot)

                    binding.etQuantity.setText("")
                    binding.etPurchasePrice.setText("")

                    binding.txtStatus.text = "Compra registrada ✅"
                }
            }
        }
    }

    private fun loadProducts() {

        lifecycleScope.launch {

            db.productDao().getAll().collect { products ->

                if (products.isNotEmpty()) {

                    val first = products.first()
                    selectedProductId = first.id
                    binding.txtSelectedProduct.text = "Producto: ${first.name}"
                }
            }
        }
    }
}
