package com.example.xbcad7319_vucadigital.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.models.OpportunityModel
import com.example.xbcad7319_vucadigital.models.ProductModel
import com.squareup.picasso.Picasso

class ProductAdapter (private var products: List<ProductModel>/*,
                          private val onEditClick: (ProductModel) -> Unit,
                          private val onDeleteClick: (ProductModel) -> Unit*/
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.productNameText)
        val valueText : TextView = itemView.findViewById(R.id.productValueText)
        val description : TextView = itemView.findViewById(R.id.displayProductDescription)
        val image : ImageView = itemView.findViewById(R.id.displayOpportunityStatusImage)

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

        Picasso.get().load(products.Image).into(holder.image)

        /*holder.moreImageView.setOnClickListener {
            showPopupMenu(holder.moreImageView, opportunity)
        }*/
    }


    override fun getItemCount(): Int = products.size

    fun updateProducts(newProduct: List<ProductModel>) {
        Log.d("OpportunityAdapter", "Updating opportunities: ${newProduct.size} items")
        products = newProduct
        notifyDataSetChanged()
    }
}