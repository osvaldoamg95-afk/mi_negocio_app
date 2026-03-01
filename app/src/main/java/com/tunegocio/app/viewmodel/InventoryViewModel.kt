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

    // LiveData para feedback
    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage

    // Productos (Flow directo desde DB)
    val allProducts: Flow<List<Product>> = db.productDao().getAll()

    fun createProduct(name: String, price: Double, isManufactured: Boolean) {
        if (name.isBlank()) {
            _statusMessage.value = "❌ El nombre no puede estar vacío"
            return
        }
        viewModelScope.launch {
            try {
                db.productDao().insert(Product(name = name, salePrice = price, isManufactured = isManufactured))
                _statusMessage.value = "✅ Producto creado: $name"
            } catch (e: Exception) {
                _statusMessage.value = "❌ Error al crear producto"
            }
        }
    }

    fun createRawMaterial(name: String) {
        if (name.isBlank()) {
            _statusMessage.value = "❌ El nombre no puede estar vacío"
            return
        }
        viewModelScope.launch {
            try {
                db.rawMaterialDao().insert(RawMaterial(name = name))
                _statusMessage.value = "✅ Materia prima creada: $name"
            } catch (e: Exception) {
                _statusMessage.value = "❌ Error al crear materia prima"
            }
        }
    }
    
    // Función suspendida para obtener stock (usada en UI)
    suspend fun getStockForProduct(productId: Int): Double {
        return db.inventoryLotDao().getTotalStock(productId) ?: 0.0
    }
}
