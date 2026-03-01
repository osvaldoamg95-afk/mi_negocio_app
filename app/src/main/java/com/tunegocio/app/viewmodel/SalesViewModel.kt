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
import java.text.SimpleDateFormat
import java.util.*

class SalesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    data class CartItem(
        val productId: Int,
        val productName: String,
        val quantity: Double,
        val price: Double
    ) {
        val subtotal: Double get() = quantity * price
    }

    private val _cart = MutableLiveData<List<CartItem>>(emptyList())
    val cart: LiveData<List<CartItem>> = _cart

    private val _totalAmount = MutableLiveData<Double>(0.0)
    val totalAmount: LiveData<Double> = _totalAmount

    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage

    var saleDate: Calendar = Calendar.getInstance()

    fun setDate(year: Int, month: Int, day: Int) {
        saleDate.set(year, month, day)
        saleDate.set(Calendar.HOUR_OF_DAY, 12) 
        saleDate.set(Calendar.MINUTE, 0)
    }
    
    fun getDateString(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(saleDate.time)
    }

    // ✅ NUEVO: Agregar con validación
    fun addToCart(productId: Int, productName: String, quantity: Double, price: Double) {
        // Validar si es materia prima (no vendible sola)
        // Necesitamos saber el tipo. Como en el spinner tenemos el nombre, 
        // haremos la validación fuerte al procesar o idealmente al cargar productos (filtrar).
        
        val currentList = _cart.value.orEmpty().toMutableList()
        currentList.add(CartItem(productId, productName, quantity, price))
        updateCart(currentList)
        _statusMessage.value = "🛒 $productName agregado"
    }

    // ✅ NUEVO: Eliminar ítem individual
    fun removeFromCart(item: CartItem) {
        val currentList = _cart.value.orEmpty().toMutableList()
        currentList.remove(item)
        updateCart(currentList)
        _statusMessage.value = "🗑️ Producto eliminado"
    }

    fun clearCart() {
        updateCart(emptyList())
        _statusMessage.value = "Carrito vaciado"
    }

    private fun updateCart(items: List<CartItem>) {
        _cart.value = items
        _totalAmount.value = items.sumOf { it.subtotal }
    }

    fun processSale() {
        val currentCart = _cart.value
        if (currentCart.isNullOrEmpty()) {
            _statusMessage.value = "Carrito vacío"
            return
        }

        viewModelScope.launch {
            try {
                // 1. Validar Stock (Lógica corregida para Manufactura)
                validateStock(currentCart)

                // 2. Procesar Venta
                var totalVenta = 0.0
                var totalCost = 0.0
                val details = mutableListOf<SaleDetail>()

                for (item in currentCart) {
                    val cost = calculateFIFOAndReduceStock(item.productId, item.quantity)
                    totalCost += cost
                    totalVenta += item.quantity * item.price

                    details.add(
                        SaleDetail(
                            saleId = 0,
                            productId = item.productId,
                            quantity = item.quantity,
                            salePrice = item.price
                        )
                    )
                }

                val profit = totalVenta - totalCost

                val sale = Sale(
                    date = saleDate.timeInMillis,
                    total = totalVenta,
                    costTotal = totalCost,
                    profit = profit
                )

                db.saleDao().insertFullSale(sale, details)

                clearCart()
                saleDate = Calendar.getInstance() 
                _statusMessage.value = "✅ Venta registrada el ${getDateString()}"

            } catch (e: Exception) {
                _statusMessage.value = "❌ ${e.message}"
            }
        }
    }

    private suspend fun validateStock(cartItems: List<CartItem>) {
        for (item in cartItems) {
            val product = db.productDao().getById(item.productId)
            
            // REGLA: No vender Materia Prima sola (si tuviéramos campo isRawMaterial en Product, 
            // pero como usas tablas separadas, Product siempre es vendible. 
            // Si RawMaterial es otra tabla, no aparecerá en el Spinner de productos, así que está seguro).

            if (product.isManufactured) {
                 // ✅ Lógica de Manufactura: Validar INSUMOS, no el producto
                 val recipe = db.recipeDao().getRecipeForProduct(item.productId)
                 if (recipe.isEmpty()) throw Exception("Producto ${product.name} es manufacturado pero NO TIENE RECETA")
                 
                 for (ing in recipe) {
                     val stock = db.inventoryLotDao().getTotalStock(ing.rawMaterialId) ?: 0.0
                     val needed = ing.quantityRequired * item.quantity
                     
                     // Buscar nombre del insumo para mensaje claro
                     // (Aquí simplificamos mensaje, idealmente query extra)
                     if (stock < needed) {
                         throw Exception("Falta insumo (ID: ${ing.rawMaterialId}) para ${product.name}")
                     }
                 }
            } else {
                // Producto normal: Validar su propio stock
                val stock = db.inventoryLotDao().getTotalStock(item.productId) ?: 0.0
                if (stock < item.quantity) {
                    throw Exception("Stock insuficiente en ${item.productName} (Disponible: $stock)")
                }
            }
        }
    }

    private suspend fun calculateFIFOAndReduceStock(productId: Int, qty: Double): Double {
        val product = db.productDao().getById(productId)
        var cost = 0.0

        if (product.isManufactured) {
            val recipe = db.recipeDao().getRecipeForProduct(productId)
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
        val lots = db.inventoryLotDao().getLotsFIFO(itemId) 

        for (lot in lots) {
            if (remaining <= 0) break
            val take = if (lot.quantity >= remaining) remaining else lot.quantity
            cost += take * lot.purchasePrice
            val newQty = lot.quantity - take
            db.inventoryLotDao().updateLot(lot.copy(quantity = newQty))
            remaining -= take
        }
        return cost
    }
}
