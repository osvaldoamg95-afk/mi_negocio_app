package com.tunegocio.app.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tunegocio.app.databinding.ActivityInventoryBinding
import com.tunegocio.app.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private val viewModel: InventoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Forzar mayúsculas en el input visualmente
        binding.etProductName.filters = arrayOf(InputFilter.AllCaps())

        setupObservers()

        binding.btnSaveProduct.setOnClickListener {
            val name = binding.etProductName.text.toString()
            val price = binding.etProductPrice.text.toString().toDoubleOrNull() ?: 0.0
            val isManufactured = binding.chkManufactured.isChecked
            
            viewModel.saveProduct(name, price, isManufactured)
            
            clearFields()
        }

        binding.btnOpenPurchase.setOnClickListener {
            startActivity(Intent(this, PurchaseActivity::class.java))
        }
        
        // Clic largo en la lista para cancelar edición (opcional)
        binding.txtProductList.setOnLongClickListener {
            viewModel.cancelEdit()
            clearFields()
            true
        }
    }

    private fun clearFields() {
        binding.etProductName.setText("")
        binding.etProductPrice.setText("")
        binding.chkManufactured.isChecked = false
        binding.btnSaveProduct.text = "GUARDAR PRODUCTO" // Reset texto botón
    }

    private fun setupObservers() {
        viewModel.statusMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            // Si estamos editando, cambiar texto del botón
            if (msg.startsWith("✏️")) {
                binding.btnSaveProduct.text = "ACTUALIZAR PRODUCTO"
            }
        }

        // ⚠️ IMPORTANTE: Para que la lista sea "tocable" y editable, 
        // necesitamos un ListView real en lugar de un TextView plano.
        // Pero para mantener tu estructura actual sin romper XML, 
        // vamos a usar un truco con Dialog o simplemente dejarlo como reporte visual.
        // Si quieres editar, lo ideal es agregar un BUSCADOR.
        
        // Por ahora, listamos texto plano (como lo tenías) pero MAYÚSCULAS.
        lifecycleScope.launch {
            viewModel.allProducts.collect { products ->
                val sb = StringBuilder("PRODUCTOS (TOQUE PARA EDITAR NO DISPONIBLE AUN EN TEXTVIEW):\n\n")
                for (p in products) {
                    val stock = viewModel.getStockForProduct(p.id)
                    val type = if (p.isManufactured) " (MANUF)" else ""
                    sb.append("${p.name}$type | $ ${p.salePrice} | STOCK: $stock\n")
                }
                binding.txtProductList.text = sb.toString()
            }
        }
    }
}
