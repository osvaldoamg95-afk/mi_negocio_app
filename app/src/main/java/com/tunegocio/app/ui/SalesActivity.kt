package com.tunegocio.app.ui

import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySalesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        loadProduct()

        binding.btnSell.setOnClickListener {

            val quantity = binding.etQuantity.text.toString().toDoubleOrNull() ?: 0.0

            if (selectedProductId != -1 && quantity > 0) {

                lifecycleScope.launch {

                    // ✅ 1️⃣ VALIDAR SI EL DÍA ESTÁ CERRADO
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

                    // ✅ 2️⃣ VALIDAR STOCK
                    val stock = db.inventoryLotDao()
                        .getTotalStock(selectedProductId) ?: 0.0

                    if (stock < quantity) {
                        binding.txtStatus.text = "❌ Stock insuficiente"
                        return@launch
                    }

                    try {

                        // ✅ 3️⃣ Obtener costo real usando FIFO
                        val cost = sellFIFO(selectedProductId, quantity)

                        val total = quantity * selectedProductPrice
                        val profit = total - cost

                        // ✅ 4️⃣ Guardar venta
                        val saleId = db.saleDao().insertSale(
                            Sale(
                                date = System.currentTimeMillis(),
                                total = total,
                                costTotal = cost,
                                profit = profit
                            )
                        )

                        // ✅ 5️⃣ Guardar detalle
                        db.saleDao().insertSaleDetail(
                            SaleDetail(
                                saleId = saleId.toInt(),
                                productId = selectedProductId,
                                quantity = quantity,
                                salePrice = selectedProductPrice
                            )
                        )

                        binding.txtStatus.text =
                            "✅ Venta registrada | Ganancia: %.2f"
                                .format(profit)

                        binding.etQuantity.setText("")

                    } catch (e: Exception) {
                        binding.txtStatus.text = "Error en venta"
                    }
                }
            }
        }
    }

    private fun loadProducts() {

    lifecycleScope.launch {

        val products = db.productDao().getAllList()

        if (products.isNotEmpty()) {

            val adapter = android.widget.ArrayAdapter(
                this@SalesActivity,
                android.R.layout.simple_spinner_item,
                products.map { it.name }
            )

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spProducts.adapter = adapter

            binding.spProducts.setOnItemSelectedListener(
                object : android.widget.AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: android.widget.AdapterView<*>?,
                        view: android.view.View?,
                        position: Int,
                        id: Long
                    ) {
                        val selected = products[position]
                        selectedProductId = selected.id
                        selectedProductPrice = selected.salePrice
                    }

                    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                }
            )
        }
    }
}

    // ✅ FIFO que devuelve el COSTO TOTAL
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
