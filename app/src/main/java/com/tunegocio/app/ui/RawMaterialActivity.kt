package com.tunegocio.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.data.entities.RawMaterial
import com.tunegocio.app.databinding.ActivityRawMaterialBinding

class RawMaterialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRawMaterialBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRawMaterialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        loadMaterials()

        binding.btnSaveMaterial.setOnClickListener {

            val name = binding.etMaterialName.text.toString()

            if (name.isNotEmpty()) {

                lifecycleScope.launch {

                    db.rawMaterialDao().insert(
                        RawMaterial(name = name)
                    )

                    binding.etMaterialName.setText("")
                    binding.txtStatus.text = "✅ Materia prima creada"

                    loadMaterials()
                }
            }
        }
    }

    private fun loadMaterials() {

        lifecycleScope.launch {

            val materials = db.rawMaterialDao().getAll()

            var text = "MATERIAS PRIMAS:\n\n"

            for (m in materials) {
                text += "- ${m.name}\n"
            }

            binding.txtMaterials.text = text
        }
    }
}
