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

                    val stock = db.inventoryLotDao()
                        .getTotalStock(selectedProductId) ?: 0.0

                    if (stock < quantity) {
                        binding.txtStatus.text = "❌ Stock insuficiente"
                        return@launch
                    }

                    try {

                        // ✅ Obtener costo real usando FIFO
                        val cost = sellFIFO(selectedProductId, quantity)

                        val total = quantity * selectedProductPrice
                        val profit = total - cost

                        // ✅ Guardar venta
                        val saleId = db.saleDao().insertSale(
                            Sale(
                                date = System.currentTimeMillis(),
                                total = total,
                                costTotal = cost,
                                profit = profit
                            )
                        )

                        // ✅ Guardar detalle
                        db.saleDao().insertSaleDetail(
                            SaleDetail(
                                saleId = saleId.toInt(),
                                productId = selectedProductId,
                                quantity = quantity,
                                salePrice = selectedProductPrice
                            )
                        )

                        binding.txtStatus.text =
                            "✅ Venta registrada | Ganancia: $profit"

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

    // ✅ FIFO que ahora devuelve el COSTO TOTAL
    private suspend fun sellFIFO(
        productId: Int,
        quantityToSell: Double
    ): Double {

        var remaining = quantityToSell
        var totalCost = 0.0

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

        return totalCost
    }
}
