package com.tunegocio.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.data.entities.Sale
import com.tunegocio.app.data.entities.SaleDetail
import com.tunegocio.app.databinding.ActivitySalesBinding

class SalesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalesBinding
    private lateinit var db: AppDatabase
    private var selectedProductId: Int = -1
    private var selectedProductPrice: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySalesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        loadFirstProduct()

        binding.btnSell.setOnClickListener {

            val quantity = binding.etQuantity.text.toString().toDoubleOrNull() ?: 0.0

            if (selectedProductId != -1 && quantity > 0) {

                lifecycleScope.launch {

                    val stock = db.inventoryLotDao().getTotalStock(selectedProductId) ?: 0.0

                    if (stock < quantity) {
                        binding.txtStatus.text = "❌ Stock insuficiente"
                        return@launch
                    }

                    try {

                        sellFIFO(selectedProductId, quantity)

                        val total = quantity * selectedProductPrice

                        val saleId = db.saleDao().insertSale(
                            Sale(
                                date = System.currentTimeMillis(),
                                total = total
                            )
                        )

                        db.saleDao().insertSaleDetail(
                            SaleDetail(
                                saleId = saleId.toInt(),
                                productId = selectedProductId,
                                quantity = quantity,
                                salePrice = selectedProductPrice
                            )
                        )

                        binding.txtStatus.text = "✅ Venta registrada"
                        binding.etQuantity.setText("")

                    } catch (e: Exception) {
                        binding.txtStatus.text = "Error en venta"
                    }
                }
            }
        }
    }

    private fun loadFirstProduct() {

        lifecycleScope.launch {

            val products = db.productDao().getAllList()

            if (products.isNotEmpty()) {

                val first = products[0]
                selectedProductId = first.id
                selectedProductPrice = first.salePrice

                binding.txtProduct.text =
                    "Producto: ${first.name} | Precio: ${first.salePrice}"
            }
        }
    }

    private suspend fun sellFIFO(productId: Int, quantityToSell: Double) {

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
