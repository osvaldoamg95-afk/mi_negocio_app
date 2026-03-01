package com.tunegocio.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tunegocio.app.databinding.ItemProductBinding
import com.tunegocio.app.viewmodel.SalesViewModel

// Reutilizamos item_product.xml porque ya tiene botón de eliminar/editar
// (Cambiamos el icono visualmente en código o creamos item_cart.xml si prefieres, 
//  pero para rápido reusamos y cambiamos icono).

class CartAdapter(
    private val onDeleteClick: (SalesViewModel.CartItem) -> Unit
) : ListAdapter<SalesViewModel.CartItem, CartAdapter.CartViewHolder>(DiffCallback) {

    inner class CartViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SalesViewModel.CartItem) {
            binding.txtName.text = item.productName
            // Mostramos cálculo: Cantidad x Precio = Subtotal
            val sub = item.quantity * item.price
            binding.txtDetails.text = "${item.quantity} x $${item.price} = $${String.format("%.2f", sub)}"
            
            // Cambiamos icono a "Basura"
            binding.btnEdit.setImageResource(android.R.drawable.ic_menu_delete)
            binding.btnEdit.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SalesViewModel.CartItem>() {
        // En carrito, items son únicos por referencia de objeto en lista
        override fun areItemsTheSame(old: SalesViewModel.CartItem, new: SalesViewModel.CartItem) = old === new
        override fun areContentsTheSame(old: SalesViewModel.CartItem, new: SalesViewModel.CartItem) = old == new
    }
}
