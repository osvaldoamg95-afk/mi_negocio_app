package com.tunegocio.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tunegocio.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVentas.setOnClickListener {
            binding.txtResultado.text = "Módulo de ventas próximamente"
        }

        binding.btnInventario.setOnClickListener {
            binding.txtResultado.text = "Módulo de inventario próximamente"
        }
    }
}
