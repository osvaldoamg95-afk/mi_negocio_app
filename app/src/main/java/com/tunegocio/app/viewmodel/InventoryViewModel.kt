package com.tunegocio.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.data.entities.Product
import com.tunegocio.app.data.entities.ProductIngredient
import com.tunegocio.app.data.entities.ProductType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage

    val allProducts: Flow<List<Product>> = db.productDao().getAllFlow()

    // Receta Temporal (ID Insumo -> Cantidad)
    private val _tempRecipe = MutableLiveData<MutableMap<Int, Double>>(mutableMapOf())
    val tempRecipe: LiveData<MutableMap<Int, Double>> = _tempRecipe
    
    // Lista de Insumos (para el Spinner)
    private val _insumosList = MutableLiveData<List<Product>>()
    val insumosList: LiveData<List<Product>> = _insumosList

    var editingProduct: Product? = null

    init {
        loadInsumos()
    }

    fun loadInsumos() {
        viewModelScope.launch {
            _insumosList.value = db.productDao().getInsumos()
        }
    }

    fun addIngredient(insumoId: Int, qty: Double) {
        val current = _tempRecipe.value ?: mutableMapOf()
        current[insumoId] = qty
        _tempRecipe.value = current
    }

    fun clearRecipe() {
        _tempRecipe.value = mutableMapOf()
    }

    fun saveProduct(name: String, price: Double, type: ProductType) {
        val upperName = name.uppercase().trim()
        if (upperName.isBlank()) {
            _statusMessage.value = "❌ Nombre vacío"
            return
        }

        viewModelScope.launch {
            try {
                // Validar duplicado
                val existing = db.productDao().getByName(upperName)
                if (existing != null && (editingProduct == null || existing.id != editingProduct!!.id)) {
                    _statusMessage.value = "❌ Ya existe: $upperName"
                    return@launch
                }

                // Si es manufacturado, validar receta
                if (type == ProductType.MANUFACTURADO && _tempRecipe.value.isNullOrEmpty()) {
                    _statusMessage.value = "❌ Producto manufacturado necesita ingredientes"
                    return@launch
                }

                if (editingProduct == null) {
                    // CREAR
                    val id = db.productDao().insert(
                        Product(name = upperName, salePrice = price, type = type)
                    )
                    
                    // Guardar ingredientes
                    if (type == ProductType.MANUFACTURADO) {
                        _tempRecipe.value?.forEach { (ingId, qty) ->
                            db.productDao().insertIngredient(ProductIngredient(id.toInt(), ingId, qty))
                        }
                    }
                    _statusMessage.value = "✅ Creado: $upperName"
                } else {
                    // EDITAR
                    val p = editingProduct!!.copy(name = upperName, salePrice = price, type = type)
                    db.productDao().update(p)
                    
                    // Actualizar ingredientes (Borrar y reinsertar)
                    if (type == ProductType.MANUFACTURADO) {
                        db.productDao().deleteIngredients(p.id)
                        _tempRecipe.value?.forEach { (ingId, qty) ->
                            db.productDao().insertIngredient(ProductIngredient(p.id, ingId, qty))
                        }
                    }
                    _statusMessage.value = "✅ Actualizado: $upperName"
                    editingProduct = null
                }
                clearRecipe()
            } catch (e: Exception) {
                _statusMessage.value = "❌ Error: ${e.message}"
            }
        }
    }
    
    // Preparar edición (cargar receta si existe)
    fun prepareEdit(product: Product) {
        editingProduct = product
        if (product.type == ProductType.MANUFACTURADO) {
            viewModelScope.launch {
                val ingredients = db.productDao().getIngredients(product.id)
                val map = mutableMapOf<Int, Double>()
                ingredients.forEach { map[it.ingredientId] = it.quantityRequired }
                _tempRecipe.value = map
            }
        } else {
            clearRecipe()
        }
        _statusMessage.value = "✏️ Editando: ${product.name}"
    }

    fun cancelEdit() {
        editingProduct = null
        clearRecipe()
        _statusMessage.value = "Cancelado"
    }
    
    suspend fun getStock(id: Int): Double {
        return db.inventoryLotDao().getTotalStock(id) ?: 0.0
    }
}
