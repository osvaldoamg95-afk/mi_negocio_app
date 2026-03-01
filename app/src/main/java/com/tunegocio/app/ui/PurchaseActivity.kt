package com.tunegocio.app.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tunegocio.app.data.AppDatabase
import com.tunegocio.app.databinding.ActivityPurchaseBinding
import com.tunegocio.app.ui.adapters.PurchaseAdapter
import com.tunegocio.app.viewmodel.PurchaseViewModel
import kotlinx.coroutines.launch

class PurchaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPurchaseBinding
    private val viewModel: PurchaseViewModel by viewModels()
    private lateinit var db: AppDatabase
    private lateinit var adapter: PurchaseAdapter

    private var selectedProductId: Int = -1
    private var selectedProductName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        setupRecyclerView()
        loadProducts()
        setupObservers()

        binding.btnAddPurchase.setOnClickListener {
            val qty = binding.etQuantity.text.toString().toDoubleOrNull() ?: 0.0
            val price = binding.etPurchasePrice.text.toString().toDoubleOrNull() ?: 0.0

            if (selectedProductId != -1 && qty > 0) {
                viewModel.addToCart(selectedProductId, selectedProductName, qty, price)
                binding.etQuantity.setText("")
                binding.etPurchasePrice.setText("")
            }
        }

        binding.btnSavePurchase.setOnClickListener {
            viewModel.savePurchase()
        }
    }

    private fun setupRecyclerView() {
        adapter = PurchaseAdapter { item ->
            viewModel.removeFromCart(item)
        }
        binding.rvPurchaseCart.layoutManager = LinearLayoutManager(this)
        binding.rvPurchaseCart.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.cart.observe(this) { items ->
            adapter.submitList(items.toList())
        }

        viewModel.totalAmount.observe(this) { total ->
            binding.txtTotalPurchase.text = "$ %.2f".format(total)
        }

        viewModel.statusMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            val products = db.productDao().getAllList()
            if (products.isNotEmpty()) {
                val adapter = ArrayAdapter(
                    this@PurchaseActivity,
                    android.R.layout.simple_spinner_item,
                    products.map { it.name }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spProducts.adapter = adapter

                binding.spProducts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val p = products[position]
                        selectedProductId = p.id
                        selectedProductName = p.name
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }
    }
}
