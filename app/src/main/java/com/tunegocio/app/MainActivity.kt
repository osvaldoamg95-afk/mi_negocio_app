package com.tunegocio.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tunegocio.app.databinding.ActivityMainBinding
import com.tunegocio.app.ui.InventoryActivity
import com.tunegocio.app.ui.SalesActivity
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnInventario.setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }

        binding.btnVentas.setOnClickListener {
    startActivity(Intent(this, SalesActivity::class.java))
}
        }
    }
}
