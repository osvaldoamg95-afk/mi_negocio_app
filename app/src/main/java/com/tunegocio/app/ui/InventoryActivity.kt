package com.tunegocio.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.data.entities.Product
import com.tunegocio.app.data.entities.InventoryLot
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

        // Ir a compras
        binding.btnOpenPurchase.setOnClickListener {
            startActivity(Intent(this, PurchaseActivity::class.java))
        }

        // ✅ BOTÓN PARA PROBAR FIFO
        binding.btnTestSell.setOnClickListener {

            lifecycleScope.launch {

                db.productDao().getAll().collect { list ->

                    if (list.isNotEmpty()) {

                        val firstProduct = list.first()

                        try {
                            sellProduct(firstProduct.id, 1.0)
                            loadProducts()
                        } catch (e: Exception) {
                            binding.txtProductList.text = "Stock insuficiente"
                        }
                    }
                }
            }
        }

        loadProducts()
    }

    // ✅ Se ejecuta cada vez que vuelves a la pantalla
    override fun onResume() {
        super.onResume()
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

    // ✅ FUNCIÓN FIFO REAL
    private suspend fun sellProduct(productId: Int, quantityToSell: Double) {

        var remaining = quantityToSell

        val lots = db.inventoryLotDao().getLotsFIFO(productId)

        for (lot in lots) {

            if (remaining <= 0) break

            if (lot.quantity <= remaining) {

                remaining -= lot.quantity

                db.inventoryLotDao().updateLot(
                    lot.copy(quantity = 0.0)
                )

            } else {

                val newQuantity = lot.quantity - remaining

                db.inventoryLotDao().updateLot(
                    lot.copy(quantity = newQuantity)
                )

                remaining = 0.0
            }
        }

        if (remaining > 0) {
            throw Exception("Stock insuficiente")
        }
    }
}
