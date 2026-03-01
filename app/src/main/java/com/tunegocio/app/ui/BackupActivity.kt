package com.tunegocio.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tunegocio.app.databinding.ActivityBackupBinding
import com.tunegocio.app.utils.BackupHelper

class BackupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackup.setOnClickListener {
            val path = BackupHelper.backupDatabase(this)
            binding.txtStatus.text = "Backup creado en:\n$path"
        }

        binding.btnRestore.setOnClickListener {
            val result = BackupHelper.restoreDatabase(this)
            binding.txtStatus.text = result
        }

        // ... código existente ...

        binding.btnTemplate.setOnClickListener {
            lifecycleScope.launch {
                val path = ImportHelper.generateTemplate()
                binding.txtStatus.text = "📄 Plantilla guardada en:\n$path\n\nEdítala con Excel y luego presiona Importar."
            }
        }

        binding.btnImport.setOnClickListener {
            lifecycleScope.launch {
                val result = ImportHelper.importData(this@BackupActivity, db) // Necesitas instanciar db aquí si no lo tienes
                binding.txtStatus.text = result
            }
        }
    }
}
