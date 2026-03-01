package com.tunegocio.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tunegocio.app.databinding.ItemProductBinding
import com.tunegocio.app.viewmodel.PurchaseViewModel

class PurchaseAdapter(
    private val onDeleteClick: (PurchaseViewModel.PurchaseItem) -> Unit
) : ListAdapter<PurchaseViewModel.PurchaseItem, PurchaseAdapter.PurchaseViewHolder>(DiffCallback) {

    inner class PurchaseViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PurchaseViewModel.PurchaseItem) {
            binding.txtName.text = item.productName
            // Mostramos Costo en lugar de Precio Venta
            binding.txtDetails.text = "${item.quantity} x $${item.price} (COSTO)"
            
            binding.btnEdit.setImageResource(android.R.drawable.ic_menu_delete)
            binding.btnEdit.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PurchaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PurchaseViewModel.PurchaseItem>() {
        override fun areItemsTheSame(old: PurchaseViewModel.PurchaseItem, new: PurchaseViewModel.PurchaseItem) = old === new
        override fun areContentsTheSame(old: PurchaseViewModel.PurchaseItem, new: PurchaseViewModel.PurchaseItem) = old == new
    }
}
