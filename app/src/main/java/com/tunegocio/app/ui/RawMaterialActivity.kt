package com.tunegocio.app.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.databinding.ActivityRawMaterialBinding
import com.tunegocio.app.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch

class RawMaterialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRawMaterialBinding
    private val viewModel: InventoryViewModel by viewModels() // Reusamos el mismo ViewModel
    private lateinit var db: AppDatabase // Para listar materiales (simple)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRawMaterialBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        db = AppDatabase.getDatabase(this)

        setupObservers()
        loadMaterials()

        binding.btnSaveMaterial.setOnClickListener {
            val name = binding.etMaterialName.text.toString()
            viewModel.createRawMaterial(name)
            binding.etMaterialName.setText("")
            loadMaterials() // Recargar lista
        }
    }

    private fun setupObservers() {
        viewModel.statusMessage.observe(this) { msg ->
            binding.txtStatus.text = msg
        }
    }

    private fun loadMaterials() {
        lifecycleScope.launch {
            val materials = db.rawMaterialDao().getAll()
            val sb = StringBuilder("MATERIAS PRIMAS:\n\n")
            materials.forEach { m -> sb.append("- ${m.name}\n") }
            binding.txtMaterials.text = sb.toString()
        }
    }
}
