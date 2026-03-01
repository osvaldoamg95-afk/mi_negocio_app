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
    )

    private val _cart = MutableLiveData<List<PurchaseItem>>(emptyList())
    val cart: LiveData<List<PurchaseItem>> = _cart

    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage

    fun addToCart(productId: Int, productName: String, quantity: Double, price: Double) {
        val currentList = _cart.value.orEmpty().toMutableList()
        currentList.add(PurchaseItem(productId, productName, quantity, price))
        _cart.value = currentList
        _statusMessage.value = "📦 $productName agregado"
    }

    fun clearCart() {
        _cart.value = emptyList()
        _statusMessage.value = "Carrito vaciado"
    }

    fun savePurchase() {
        val currentCart = _cart.value
        if (currentCart.isNullOrEmpty()) {
            _statusMessage.value = "Carrito vacío"
            return
        }

        viewModelScope.launch {
            try {
                // Insertamos cada lote. 
                // IDEALMENTE: Deberíamos tener un 'insertAll' en el DAO para hacerlo en una sola transacción.
                // Por ahora lo hacemos en bucle dentro de la corrutina.
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
                clearCart()
                _statusMessage.value = "✅ Compra guardada exitosamente"
            } catch (e: Exception) {
                _statusMessage.value = "❌ Error al guardar compra: ${e.message}"
            }
        }
    }
}
