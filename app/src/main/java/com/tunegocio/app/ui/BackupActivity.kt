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
    }
}
