package com.tunegocio.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tunegocio.app.data.entities.Product
import com.tunegocio.app.databinding.ItemProductBinding

class ProductAdapter(
    private val onEditClick: (Product) -> Unit,
    private val stockProvider: (Int) -> Double // Función para pedir el stock
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(DiffCallback) {

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.txtName.text = product.name
            
            val typeStr = when(product.type) {
                ProductType.INSUMO -> "🔵 INSUMO"
                ProductType.PRODUCTO_SIMPLE -> "🟢 SIMPLE"
                ProductType.MANUFACTURADO -> "🟠 MANUFACTURA"
            }
            binding.txtDetails.text = "$typeStr | Stock: $stock | $${product.salePrice}"

            binding.btnEdit.setOnClickListener {
                onEditClick(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
    }
}
