package com.tunegocio.app.ui

import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tunegocio.app.data.entities.ProductType
import com.tunegocio.app.data.entities.Product
import com.tunegocio.app.databinding.ActivityInventoryBinding
import com.tunegocio.app.ui.adapters.ProductAdapter
import com.tunegocio.app.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private val viewModel: InventoryViewModel by viewModels()
    private lateinit var adapter: ProductAdapter
    
    // Para el spinner de ingredientes
    private var selectedIngredientId: Int = -1
    private var insumosList: List<Product> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etName.filters = arrayOf(InputFilter.AllCaps())

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        // RadioGroup Listener
        binding.rgType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.rbInsumo.id -> {
                    binding.etPrice.isEnabled = false
                    binding.etPrice.setText("0.0")
                    binding.layoutRecipe.visibility = View.GONE
                }
                binding.rbSimple.id -> {
                    binding.etPrice.isEnabled = true
                    binding.layoutRecipe.visibility = View.GONE
                }
                binding.rbManufacturado.id -> {
                    binding.etPrice.isEnabled = true
                    binding.layoutRecipe.visibility = View.VISIBLE
                    viewModel.loadInsumos() // Recargar lista
                }
            }
        }

        // Agregar Ingrediente
        binding.btnAddIngredient.setOnClickListener {
            val qty = binding.etIngredientQty.text.toString().toDoubleOrNull() ?: 0.0
            if (selectedIngredientId != -1 && qty > 0) {
                viewModel.addIngredient(selectedIngredientId, qty)
                binding.etIngredientQty.setText("")
            }
        }

        // Guardar
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString()
            val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
            
            val type = when (binding.rgType.checkedRadioButtonId) {
                binding.rbInsumo.id -> ProductType.INSUMO
                binding.rbSimple.id -> ProductType.PRODUCTO_SIMPLE
                binding.rbManufacturado.id -> ProductType.MANUFACTURADO
                else -> null
            }

            if (type != null) {
                viewModel.saveProduct(name, price, type)
                clearFields()
            } else {
                Toast.makeText(this, "Seleccione un tipo", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.btnCancelEdit.setOnClickListener {
            viewModel.cancelEdit()
            clearFields()
        }

        // RecyclerView
        adapter = ProductAdapter(
            onEditClick = { product ->
                viewModel.prepareEdit(product)
                binding.etName.setText(product.name)
                binding.etPrice.setText(product.salePrice.toString())
                
                when(product.type) {
                    ProductType.INSUMO -> binding.rbInsumo.isChecked = true
                    ProductType.PRODUCTO_SIMPLE -> binding.rbSimple.isChecked = true
                    ProductType.MANUFACTURADO -> binding.rbManufacturado.isChecked = true
                }
                
                binding.btnSave.text = "ACTUALIZAR"
                binding.btnCancelEdit.visibility = View.VISIBLE
            },
            stockProvider = { id -> 
                var s = 0.0
                runBlocking { s = viewModel.getStock(id) }
                s
            }
        )
        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = adapter
    }

    private fun clearFields() {
        binding.etName.setText("")
        binding.etPrice.setText("")
        binding.rgType.clearCheck()
        binding.layoutRecipe.visibility = View.GONE
        binding.btnSave.text = "GUARDAR PRODUCTO"
        binding.btnCancelEdit.visibility = View.GONE
    }

    private fun setupObservers() {
        viewModel.statusMessage.observe(this) { 
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show() 
        }

        lifecycleScope.launch {
            viewModel.allProducts.collect { adapter.submitList(it) }
        }
        
        // Cargar Spinner Insumos
        viewModel.insumosList.observe(this) { list ->
            insumosList = list
            val names = list.map { it.name }
            val spinAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
            spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spIngredients.adapter = spinAdapter
            
            binding.spIngredients.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                    selectedIngredientId = list[pos].id
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }
        
        // Mostrar Receta Temporal
        viewModel.tempRecipe.observe(this) { map ->
            if (map.isEmpty()) {
                binding.txtRecipeList.text = "Sin ingredientes."
            } else {
                val sb = StringBuilder()
                map.forEach { (id, qty) ->
                    // Buscar nombre (lento pero funcional para UI pequeña)
                    val name = insumosList.find { it.id == id }?.name ?: "ID:$id"
                    sb.append("- $name x $qty\n")
                }
                binding.txtRecipeList.text = sb.toString()
            }
        }
    }
}
