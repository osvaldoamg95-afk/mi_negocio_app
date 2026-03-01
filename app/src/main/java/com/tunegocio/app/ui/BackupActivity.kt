package com.tunegocio.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.databinding.ActivityBackupBinding
import com.tunegocio.app.utils.BackupHelper
import com.tunegocio.app.utils.ImportHelper // ✅ Importante

class BackupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackupBinding
    
    // ✅ Inicializamos la DB aquí para poder pasarla al ImportHelper
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón Backup (Existente)
        binding.btnBackup.setOnClickListener {
            val path = BackupHelper.backupDatabase(this)
            binding.txtStatus.text = "✅ Backup creado en:\n$path"
        }

        // Botón Restaurar (Existente)
        binding.btnRestore.setOnClickListener {
            val result = BackupHelper.restoreDatabase(this)
            binding.txtStatus.text = result
        }

        // ✅ NUEVO: Botón Generar Plantilla
        binding.btnTemplate.setOnClickListener {
            lifecycleScope.launch {
                val path = ImportHelper.generateTemplate()
                binding.txtStatus.text = "📄 Plantilla guardada en:\n$path\n\nEdítala y luego usa Importar."
            }
        }

        // ✅ NUEVO: Botón Importar Datos
        binding.btnImport.setOnClickListener {
            lifecycleScope.launch {
                binding.txtStatus.text = "⏳ Importando..."
                val result = ImportHelper.importData(this@BackupActivity, db)
                binding.txtStatus.text = result
            }
        }
    }
}
