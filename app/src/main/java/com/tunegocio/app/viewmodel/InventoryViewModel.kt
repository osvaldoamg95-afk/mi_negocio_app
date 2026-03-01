package com.tunegocio.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.data.entities.InventoryLot
import com.tunegocio.app.data.entities.Product
import com.tunegocio.app.data.entities.RawMaterial
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage

    val allProducts: Flow<List<Product>> = db.productDao().getAll()

    var editingProduct: Product? = null

    fun saveProduct(name: String, price: Double, isManufactured: Boolean) {
        val upperName = name.uppercase().trim()

        if (upperName.isBlank()) {
            _statusMessage.value = "❌ EL NOMBRE NO PUEDE ESTAR VACÍO"
            return
        }

        viewModelScope.launch {
            try {
                // ✅ VALIDACIÓN DE DUPLICIDAD
                val existing = db.productDao().getByName(upperName)
                
                if (editingProduct == null) {
                    // Creación
                    if (existing != null) {
                        _statusMessage.value = "❌ YA EXISTE UN PRODUCTO CON ESE NOMBRE"
                        return@launch
                    }
                    db.productDao().insert(
                        Product(name = upperName, salePrice = price, isManufactured = isManufactured)
                    )
                    _statusMessage.value = "✅ PRODUCTO CREADO: $upperName"
                } else {
                    // Edición
                    if (existing != null && existing.id != editingProduct!!.id) {
                        _statusMessage.value = "❌ YA EXISTE OTRO PRODUCTO CON ESE NOMBRE"
                        return@launch
                    }
                    val updated = editingProduct!!.copy(
                        name = upperName,
                        salePrice = price,
                        isManufactured = isManufactured
                    )
                    db.productDao().update(updated)
                    _statusMessage.value = "✅ PRODUCTO ACTUALIZADO: $upperName"
                    editingProduct = null
                }
            } catch (e: Exception) {
                _statusMessage.value = "❌ ERROR AL GUARDAR"
            }
        }
    }

    // ✅ NUEVO: ELIMINAR PRODUCTO
    fun deleteProduct() {
        val product = editingProduct
        if (product == null) return

        viewModelScope.launch {
            try {
                // Verificar si tiene ventas asociadas (Integridad)
                // Como tenemos Foreign Keys, si borramos el producto, se borran sus lotes.
                // Pero si hay ventas históricas, Room podría impedir borrar (RESTRICT) o borrar en cascada.
                // Lo seguro es intentar y capturar error.
                db.productDao().delete(product)
                _statusMessage.value = "🗑️ PRODUCTO ELIMINADO"
                editingProduct = null
            } catch (e: Exception) {
                _statusMessage.value = "❌ NO SE PUEDE ELIMINAR (TIENE HISTORIAL)"
            }
        }
    }

    // ✅ NUEVO: REGISTRAR MERMA
    // La merma se registra como una "salida" negativa en inventario o simplemente reduciendo lote.
    // Lo profesional es reducir lote y registrar gasto por "Pérdida de Inventario".
    fun registerMerma(quantity: Double, reason: String) {
        val product = editingProduct
        if (product == null || quantity <= 0) return

        viewModelScope.launch {
            try {
                var remaining = quantity
                var costLost = 0.0
                
                val lots = db.inventoryLotDao().getLotsFIFO(product.id)
                
                for (lot in lots) {
                    if (remaining <= 0) break
                    val take = if (lot.quantity >= remaining) remaining else lot.quantity
                    costLost += take * lot.purchasePrice
                    
                    db.inventoryLotDao().updateLot(lot.copy(quantity = lot.quantity - take))
                    remaining -= take
                }

                if (remaining > 0) {
                    _statusMessage.value = "⚠️ MERMA PARCIAL (NO HABÍA SUFICIENTE STOCK)"
                } else {
                    _statusMessage.value = "✅ MERMA REGISTRADA"
                }

                // Opcional: Registrar en Gastos automáticamente
                // db.expenseDao().insert(Expense(..., "MERMA: $reason", costLost, ...))
            } catch (e: Exception) {
                _statusMessage.value = "❌ ERROR AL REGISTRAR MERMA"
            }
        }
    }

    fun selectProductForEdit(product: Product) {
        editingProduct = product
        _statusMessage.value = "✏️ EDITANDO: ${product.name}"
    }

    fun cancelEdit() {
        editingProduct = null
        _statusMessage.value = "OPERACIÓN CANCELADA"
    }

    fun createRawMaterial(name: String) {
        val upperName = name.uppercase().trim()
        if (upperName.isBlank()) return
        viewModelScope.launch {
            db.rawMaterialDao().insert(RawMaterial(name = upperName))
            _statusMessage.value = "✅ MATERIA PRIMA CREADA"
        }
    }
    
    suspend fun getStockForProduct(productId: Int): Double {
        return db.inventoryLotDao().getTotalStock(productId) ?: 0.0
    }
}
