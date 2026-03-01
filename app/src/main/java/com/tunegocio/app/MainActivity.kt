package com.tunegocio.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tunegocio.app.databinding.ActivityMainBinding
import com.tunegocio.app.ui.InventoryActivity
import com.tunegocio.app.ui.SalesActivity
import com.tunegocio.app.ui.ReportsActivity
import com.tunegocio.app.ui.ExpenseActivity
import com.tunegocio.app.ui.RawMaterialActivity
import com.tunegocio.app.ui.RecipeActivity
import com.tunegocio.app.ui.HistoryActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ir a Inventario
        binding.btnInventario.setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }

        // Ir a Ventas
        binding.btnVentas.setOnClickListener {
            startActivity(Intent(this, SalesActivity::class.java))
        }

        // Ir a Reportes
        binding.btnReportes.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        binding.btnGastos.setOnClickListener {
            startActivity(Intent(this, ExpenseActivity::class.java))
        }

        binding.btnMaterias.setOnClickListener {
            startActivity(Intent(this, RawMaterialActivity::class.java))
        }

        binding.btnRecetas.setOnClickListener {
            startActivity(Intent(this, RecipeActivity::class.java))
        }

        binding.btnHistorial.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }
}
