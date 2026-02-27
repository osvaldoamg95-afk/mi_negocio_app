package com.tunegocio.app.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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

    data class PurchaseItem(
        val productId: Int,
        val productName: String,
        val quantity: Double,
        val price: Double
    )

    private val purchaseCart = mutableListOf<PurchaseItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPurchaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        loadProducts()

        // ✅ Agregar al carrito de compras
        binding.btnAddPurchase.setOnClickListener {

            val quantity = binding.etQuantity.text.toString().toDoubleOrNull() ?: 0.0
            val price = binding.etPurchasePrice.text.toString().toDoubleOrNull() ?: 0.0

            if (selectedProductId != -1 && quantity > 0) {

                purchaseCart.add(
                    PurchaseItem(
                        productId = selectedProductId,
                        productName = binding.spProducts.selectedItem.toString(),
                        quantity = quantity,
                        price = price
                    )
                )

                binding.txtStatus.text = "Producto agregado a compra"
                binding.etQuantity.setText("")
                binding.etPurchasePrice.setText("")

                updatePurchaseCartView()
            }
        }

        // ✅ Guardar compra completa
        binding.btnSavePurchase.setOnClickListener {

            lifecycleScope.launch {

                for (item in purchaseCart) {

                    db.inventoryLotDao().insert(
                        InventoryLot(
                            productId = item.productId,
                            quantity = item.quantity,
                            purchasePrice = item.price,
                            purchaseDate = System.currentTimeMillis()
                        )
                    )
                }

                purchaseCart.clear()
                updatePurchaseCartView()
                binding.txtStatus.text = "✅ Compra completa registrada"
            }
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

                adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
                )

                binding.spProducts.adapter = adapter

                binding.spProducts.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            selectedProductId = products[position].id
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
            }
        }
    }

    private fun updatePurchaseCartView() {

        if (purchaseCart.isEmpty()) {
            binding.txtPurchaseCart.text = "Carrito vacío"
            return
        }

        var text = "📦 COMPRAS:\n\n"

        for ((index, item) in purchaseCart.withIndex()) {
            text += "${index + 1}. ${item.productName} x ${item.quantity}\n"
        }

        binding.txtPurchaseCart.text = text
    }
}
