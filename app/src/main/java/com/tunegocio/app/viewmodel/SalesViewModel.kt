package com.tunegocio.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.data.entities.Sale
import com.tunegocio.app.data.entities.SaleDetail
import kotlinx.coroutines.launch
import java.util.Calendar

class SalesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    // Estado del carrito
    data class CartItem(
        val productId: Int,
        val productName: String,
        val quantity: Double,
        val price: Double
    )

    private val _cart = MutableLiveData<List<CartItem>>(emptyList())
    val cart: LiveData<List<CartItem>> = _cart

    // Mensajes para la UI
    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage

    fun addToCart(productId: Int, productName: String, quantity: Double, price: Double) {
        val currentList = _cart.value.orEmpty().toMutableList()
        currentList.add(CartItem(productId, productName, quantity, price))
        _cart.value = currentList
        _statusMessage.value = "🛒 $productName agregado"
    }

    fun clearCart() {
        _cart.value = emptyList()
        _statusMessage.value = "Carrito vaciado"
    }

    fun processSale() {
        val currentCart = _cart.value
        if (currentCart.isNullOrEmpty()) {
            _statusMessage.value = "Carrito vacío"
            return
        }

        viewModelScope.launch {
            try {
                // 1. Validar Cierre Diario
                if (isDayClosed()) {
                    throw Exception("El día está cerrado")
                }

                // 2. Validar Stock Inicial
                validateStock(currentCart)

                // 3. Calcular Costos y Descontar FIFO
                var totalVenta = 0.0
                var totalCost = 0.0
                val details = mutableListOf<SaleDetail>()

                for (item in currentCart) {
                    val cost = calculateFIFOAndReduceStock(item.productId, item.quantity)
                    totalCost += cost
                    totalVenta += item.quantity * item.price

                    details.add(
                        SaleDetail(
                            saleId = 0, // Se asigna al insertar
                            productId = item.productId,
                            quantity = item.quantity,
                            salePrice = item.price
                        )
                    )
                }

                val profit = totalVenta - totalCost

                // 4. Guardar Venta Transaccional
                val sale = Sale(
                    date = System.currentTimeMillis(),
                    total = totalVenta,
                    costTotal = totalCost,
                    profit = profit
                )

                db.saleDao().insertFullSale(sale, details)

                clearCart()
                _statusMessage.value = "✅ Venta completa | Ganancia: %.2f".format(profit)

            } catch (e: Exception) {
                _statusMessage.value = "❌ ${e.message}"
            }
        }
    }

    private suspend fun isDayClosed(): Boolean {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val end = calendar.timeInMillis
        return db.dailyCloseDao().isClosed(start, end) > 0
    }

    private suspend fun validateStock(cartItems: List<CartItem>) {
        for (item in cartItems) {
            val stock = db.inventoryLotDao().getTotalStock(item.productId) ?: 0.0
            if (stock < item.quantity) {
                throw Exception("Stock insuficiente en ${item.productName}")
            }
        }
    }

    // Lógica FIFO + Manufactura
    private suspend fun calculateFIFOAndReduceStock(productId: Int, qty: Double): Double {
        val product = db.productDao().getById(productId)
        var cost = 0.0

        if (product.isManufactured) {
            val recipe = db.recipeDao().getRecipeForProduct(productId)
            if (recipe.isEmpty()) throw Exception("Producto manufacturado sin receta")

            for (ingredient in recipe) {
                val requiredQty = ingredient.quantityRequired * qty
                cost += reduceStockFIFO(ingredient.rawMaterialId, requiredQty)
            }
        } else {
            cost = reduceStockFIFO(productId, qty)
        }
        return cost
    }

    private suspend fun reduceStockFIFO(itemId: Int, qtyRequired: Double): Double {
        var remaining = qtyRequired
        var cost = 0.0
        
        // Obtenemos solo lotes con stock > 0
        val lots = db.inventoryLotDao().getLotsFIFO(itemId) 

        for (lot in lots) {
            if (remaining <= 0) break

            val take = if (lot.quantity >= remaining) remaining else lot.quantity
            
            cost += take * lot.purchasePrice
            
            val newQty = lot.quantity - take
            db.inventoryLotDao().updateLot(lot.copy(quantity = newQty))
            
            remaining -= take
        }

        if (remaining > 0.001) { // Margen de error flotante
            throw Exception("Insumo insuficiente (ID: $itemId)")
        }
        return cost
    }
}
