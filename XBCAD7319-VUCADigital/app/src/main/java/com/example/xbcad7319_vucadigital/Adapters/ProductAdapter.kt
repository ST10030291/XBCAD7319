package com.example.xbcad7319_vucadigital.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.models.OpportunityModel
import com.example.xbcad7319_vucadigital.models.ProductModel
import com.squareup.picasso.Picasso

class ProductAdapter (private var products: MutableList<ProductModel> = mutableListOf(),
                          private val onEditClick: (ProductModel) -> Unit,
                          private val onDeleteClick: (ProductModel) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.productNameText)
        val valueText : TextView = itemView.findViewById(R.id.productValueText)
        val description : TextView = itemView.findViewById(R.id.displayProductDescription)

        val moreImageView: ImageView = itemView.findViewById(R.id.more_image_view)
        //val editButton: Button = itemView.findViewById(R.id.task_edit_button)
        // val deleteButton: Button = itemView.findViewById(R.id.task_delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_item, parent, false)
        return ProductViewHolder(view)
    }


    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val products = products[position]
        holder.productName.text = products.ProductName
        holder.valueText.text = "R" + products.Price.toString()
        holder.description.text = products.Description // For now
        //holder.customerName.text = products.CustomerName // For now


        holder.moreImageView.setOnClickListener {
            showPopupMenu(holder.moreImageView, products)
        }
    }


    override fun getItemCount(): Int = products.size

    private fun showPopupMenu(view: View, product: ProductModel) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.menu_items, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit_item -> {
                    // Call the edit click listener
                    // Log.d("INF355", "Edit button called")
                    onEditClick(product)
                    true
                }
                R.id.delete_item -> {
                    // Call the delete click listener
                    onDeleteClick(product)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
    fun removeProduct(product: ProductModel) {
        val position = products.indexOfFirst { it.id == product.id }
        if (position != -1) {
            // Remove the opportunity at the found position
            products.removeAt(position)  // Change this line to use 'opportunities' list
            // Notify that an item was removed
            notifyItemRemoved(position)
        }
    }
    fun updateProduct(updatedProduct: ProductModel) {
        val index = products.indexOfFirst { it.id == updatedProduct.id }
        if (index != -1) {
            products[index] = updatedProduct
            notifyItemChanged(index) // Notify the adapter of the item change
        }
    }

    fun updateProducts(newProduct: List<ProductModel>) {
        Log.d("OpportunityAdapter", "Updating opportunities: ${newProduct.size} items")
        products = newProduct.toMutableList()
        notifyDataSetChanged()
    }
}