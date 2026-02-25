package com.tunegocio.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.tunegocio.app.databinding.ActivityMainBinding
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.data.entities.Product
import android.content.Intent
import com.tunegocio.app.ui.InventoryActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Activar ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Abrir base de datos
        val db = AppDatabase.getDatabase(this)

        // Botón Inventario
       binding.btnInventario.setOnClickListener {
    startActivity(Intent(this, InventoryActivity::class.java))
}

            lifecycleScope.launch {

                val product = Product(
                    name = "Producto Prueba",
                    salePrice = 10.0
                )

                db.productDao().insert(product)

                binding.txtResultado.text = "Producto guardado en BD"
            }
        }

        // Botón Ventas (de momento solo mensaje)
        binding.btnVentas.setOnClickListener {
            binding.txtResultado.text = "Módulo ventas próximamente"
        }
    }
}
