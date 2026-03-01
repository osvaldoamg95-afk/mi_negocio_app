package com.tunegocio.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.data.entities.Product
import com.tunegocio.app.data.entities.RawMaterial
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage

    val allProducts: Flow<List<Product>> = db.productDao().getAll()

    // Para modo edición
    var editingProduct: Product? = null

    fun saveProduct(name: String, price: Double, isManufactured: Boolean) {
        val upperName = name.uppercase().trim() // ✅ Forzar Mayúsculas

        if (upperName.isBlank()) {
            _statusMessage.value = "❌ EL NOMBRE NO PUEDE ESTAR VACÍO"
            return
        }

        viewModelScope.launch {
            try {
                if (editingProduct == null) {
                    // Crear nuevo
                    db.productDao().insert(
                        Product(name = upperName, salePrice = price, isManufactured = isManufactured)
                    )
                    _statusMessage.value = "✅ PRODUCTO CREADO: $upperName"
                } else {
                    // Actualizar existente
                    val updated = editingProduct!!.copy(
                        name = upperName,
                        salePrice = price,
                        isManufactured = isManufactured
                    )
                    db.productDao().update(updated)
                    _statusMessage.value = "✅ PRODUCTO ACTUALIZADO: $upperName"
                    editingProduct = null // Salir de modo edición
                }
            } catch (e: Exception) {
                _statusMessage.value = "❌ ERROR AL GUARDAR"
            }
        }
    }

    // Preparar UI para editar
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
        if (upperName.isBlank()) {
            _statusMessage.value = "❌ NOMBRE VACÍO"
            return
        }
        viewModelScope.launch {
            try {
                db.rawMaterialDao().insert(RawMaterial(name = upperName))
                _statusMessage.value = "✅ MATERIA PRIMA CREADA: $upperName"
            } catch (e: Exception) {
                _statusMessage.value = "❌ ERROR AL CREAR MATERIA PRIMA"
            }
        }
    }
    
    suspend fun getStockForProduct(productId: Int): Double {
        return db.inventoryLotDao().getTotalStock(productId) ?: 0.0
    }
}
