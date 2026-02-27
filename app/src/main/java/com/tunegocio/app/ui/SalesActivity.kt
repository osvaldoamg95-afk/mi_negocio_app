package com.tunegocio.app.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.data.entities.Sale
import com.tunegocio.app.data.entities.SaleDetail
import com.tunegocio.app.databinding.ActivitySalesBinding
import java.util.Calendar

class SalesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalesBinding
    private lateinit var db: AppDatabase

    private var selectedProductId: Int = -1
    private var selectedProductPrice: Double = 0.0
    private var selectedProductName: String = ""

    data class CartItem(
        val productId: Int,
        val productName: String,
        val quantity: Double,
        val price: Double
    )

    private val cart = mutableListOf<CartItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySalesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        loadProducts()

        // ✅ Agregar al carrito
        binding.btnAddToCart.setOnClickListener {

            val quantity = binding.etQuantity.text.toString().toDoubleOrNull() ?: 0.0

            if (selectedProductId != -1 && quantity > 0) {

                cart.add(
                    CartItem(
                        productId = selectedProductId,
                        productName = selectedProductName,
                        quantity = quantity,
                        price = selectedProductPrice
                    )
                )

                binding.txtStatus.text = "🛒 $selectedProductName agregado"
                binding.etQuantity.setText("")
                updateCartView()
            }
        }

        // ✅ Vaciar carrito
        binding.btnClearCart.setOnClickListener {
            cart.clear()
            updateCartView()
            binding.txtStatus.text = "Carrito vaciado"
        }

        // ✅ Cerrar venta
        binding.btnSell.setOnClickListener {

            lifecycleScope.launch {

                if (cart.isEmpty()) {
                    binding.txtStatus.text = "Carrito vacío"
                    return@launch
                }

                // ✅ Validar cierre diario
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val startDay = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val endDay = calendar.timeInMillis

                val isClosed = db.dailyCloseDao()
                    .isClosed(startDay, endDay)

                if (isClosed > 0) {
                    binding.txtStatus.text = "❌ El día está cerrado"
                    return@launch
                }

                // ✅ Validar stock
                for (item in cart) {

                    val stock = db.inventoryLotDao()
                        .getTotalStock(item.productId) ?: 0.0

                    if (stock < item.quantity) {
                        binding.txtStatus.text =
                            "❌ Stock insuficiente en ${item.productName}"
                        return@launch
                    }
                }

                var totalVenta = 0.0
                var totalCost = 0.0

                for (item in cart) {

                    val cost = sellFIFO(item.productId, item.quantity)
                    totalCost += cost
                    totalVenta += item.quantity * item.price
                }

                val profit = totalVenta - totalCost

                val saleId = db.saleDao().insertSale(
                    Sale(
                        date = System.currentTimeMillis(),
                        total = totalVenta,
                        costTotal = totalCost,
                        profit = profit
                    )
                )

                for (item in cart) {

                    db.saleDao().insertSaleDetail(
                        SaleDetail(
                            saleId = saleId.toInt(),
                            productId = item.productId,
                            quantity = item.quantity,
                            salePrice = item.price
                        )
                    )
                }

                cart.clear()
                updateCartView()

                binding.txtStatus.text =
                    "✅ Venta completa | Ganancia: %.2f".format(profit)
            }
        }
    }

    private fun updateCartView() {

        if (cart.isEmpty()) {
            binding.txtCart.text = "Carrito vacío"
            return
        }

        var text = "🛒 CARRITO:\n\n"

        for ((index, item) in cart.withIndex()) {
            text += "${index + 1}. ${item.productName} x ${item.quantity}\n"
        }

        binding.txtCart.text = text
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
                            val selected = products[position]
                            selectedProductId = selected.id
                            selectedProductPrice = selected.salePrice
                            selectedProductName = selected.name
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
            }
        }
    }

    private suspend fun sellFIFO(
        productId: Int,
        quantityToSell: Double
    ): Double {

         var totalCost = 0.0

         val product = db.productDao().getById(productId)

         if (product.isManufactured) {

             val recipe = db.recipeDao().getRecipeForProduct(productId)

             if (recipe.isEmpty()) {
            throw Exception("Producto manufacturado sin receta")
            }

            // ✅ Validar stock de materias primas primero
            for (item in recipe) {

                 val requiredQty = item.quantityRequired * quantityToSell

                 val stock = db.inventoryLotDao()
                       .getTotalStock(item.rawMaterialId) ?: 0.0

                 if (stock < requiredQty) {
                     throw Exception("Materia prima insuficiente")
                 }
           }

           // ✅ Descontar materias primas
           for (item in recipe) {

               var remaining = item.quantityRequired * quantityToSell

               val lots = db.inventoryLotDao()
                   .getLotsFIFO(item.rawMaterialId)

               for (lot in lots) {

                   if (remaining <= 0) break

                   if (lot.quantity <= remaining) {

                       totalCost += lot.quantity * lot.purchasePrice
                       remaining -= lot.quantity

                       db.inventoryLotDao().updateLot(
                           lot.copy(quantity = 0.0)
                       )

                   } else {

                       totalCost += remaining * lot.purchasePrice

                       db.inventoryLotDao().updateLot(
                           lot.copy(quantity = lot.quantity - remaining)
                       )

                       remaining = 0.0
                   }
               }
           }

       } else {

           var remaining = quantityToSell

           val lots = db.inventoryLotDao().getLotsFIFO(productId)

           for (lot in lots) {

               if (remaining <= 0) break

               if (lot.quantity <= remaining) {

                   totalCost += lot.quantity * lot.purchasePrice
                   remaining -= lot.quantity

                   db.inventoryLotDao().updateLot(
                       lot.copy(quantity = 0.0)
                )

            } else {

                totalCost += remaining * lot.purchasePrice

                db.inventoryLotDao().updateLot(
                   lot.copy(quantity = lot.quantity - remaining)
                )

                remaining = 0.0
            }
        }

        if (remaining > 0) {
            throw Exception("Stock insuficiente")
        }
    }

    return totalCost
  }
}
