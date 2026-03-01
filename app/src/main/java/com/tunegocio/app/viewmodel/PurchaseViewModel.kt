package com.tunegocio.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.data.entities.InventoryLot
import kotlinx.coroutines.launch

class PurchaseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    data class PurchaseItem(
        val productId: Int,
        val productName: String,
        val quantity: Double,
        val price: Double
    ) {
        val subtotal: Double get() = quantity * price
    }

    private val _cart = MutableLiveData<List<PurchaseItem>>(emptyList())
    val cart: LiveData<List<PurchaseItem>> = _cart

    private val _totalAmount = MutableLiveData<Double>(0.0)
    val totalAmount: LiveData<Double> = _totalAmount

    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage

    fun addToCart(productId: Int, productName: String, quantity: Double, price: Double) {
        val currentList = _cart.value.orEmpty().toMutableList()
        currentList.add(PurchaseItem(productId, productName, quantity, price))
        updateCart(currentList)
        _statusMessage.value = "📦 $productName agregado"
    }

    fun removeFromCart(item: PurchaseItem) {
        val currentList = _cart.value.orEmpty().toMutableList()
        currentList.remove(item)
        updateCart(currentList)
    }

    private fun updateCart(items: List<PurchaseItem>) {
        _cart.value = items
        _totalAmount.value = items.sumOf { it.subtotal }
    }

    fun savePurchase() {
        val currentCart = _cart.value
        if (currentCart.isNullOrEmpty()) {
            _statusMessage.value = "Carrito vacío"
            return
        }

        viewModelScope.launch {
            try {
                currentCart.forEach { item ->
                    db.inventoryLotDao().insert(
                        InventoryLot(
                            productId = item.productId,
                            quantity = item.quantity,
                            purchasePrice = item.price,
                            purchaseDate = System.currentTimeMillis()
                        )
                    )
                }
                updateCart(emptyList()) // Limpiar
                _statusMessage.value = "✅ Compra guardada exitosamente"
            } catch (e: Exception) {
                _statusMessage.value = "❌ Error al guardar compra: ${e.message}"
            }
        }
    }
}
