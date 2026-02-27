package com.tunegocio.app.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.data.entities.Recipe
import com.tunegocio.app.databinding.ActivityRecipeBinding

class RecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeBinding
    private lateinit var db: AppDatabase

    private var selectedProductId: Int = -1
    private var selectedMaterialId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        loadProducts()
        loadMaterials()
        loadRecipes()

        binding.btnAddRecipe.setOnClickListener {

            val quantity = binding.etQuantityRequired.text.toString()
                .toDoubleOrNull() ?: 0.0

            if (selectedProductId != -1 &&
                selectedMaterialId != -1 &&
                quantity > 0
            ) {

                lifecycleScope.launch {

                    db.recipeDao().insert(
                        Recipe(
                            productId = selectedProductId,
                            rawMaterialId = selectedMaterialId,
                            quantityRequired = quantity
                        )
                    )

                    binding.txtStatus.text = "✅ Receta guardada"
                    binding.etQuantityRequired.setText("")

                    loadRecipes()
                }
            }
        }
    }

    private fun loadProducts() {

        lifecycleScope.launch {

            val products = db.productDao().getAllList()

            val adapter = ArrayAdapter(
                this@RecipeActivity,
                android.R.layout.simple_spinner_item,
                products.map { it.name }
            )

            adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )

            binding.spProduct.adapter = adapter

            binding.spProduct.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedProductId = products[position].id
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
        }
    }

    private fun loadMaterials() {

        lifecycleScope.launch {

            val materials = db.rawMaterialDao().getAll()

            val adapter = ArrayAdapter(
                this@RecipeActivity,
                android.R.layout.simple_spinner_item,
                materials.map { it.name }
            )

            adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )

            binding.spMaterial.adapter = adapter

            binding.spMaterial.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedMaterialId = materials[position].id
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
        }
    }

    private fun loadRecipes() {

        lifecycleScope.launch {

            val products = db.productDao().getAllList()
            val materials = db.rawMaterialDao().getAll()

            var text = "RECETAS:\n\n"

            for (product in products) {

                val recipeList = db.recipeDao()
                    .getRecipeForProduct(product.id)

                if (recipeList.isNotEmpty()) {

                    text += "Producto: ${product.name}\n"

                    for (r in recipeList) {

                        val materialName = materials
                            .find { it.id == r.rawMaterialId }
                            ?.name ?: "Desconocido"

                        text += "  - $materialName x ${r.quantityRequired}\n"
                    }

                    text += "\n"
                }
            }

            binding.txtStatus.text = text
        }
    }
}
