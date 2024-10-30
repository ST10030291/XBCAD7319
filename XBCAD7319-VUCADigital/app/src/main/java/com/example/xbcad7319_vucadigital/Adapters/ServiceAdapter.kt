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
import com.example.xbcad7319_vucadigital.models.TaskModel
import com.squareup.picasso.Picasso

//only displays services
class ServiceAdapter  (private var services: MutableList<ProductModel> = mutableListOf(),
                          private val onEditClick: (ProductModel) -> Unit,
                          private val onDeleteClick: (ProductModel) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {
    class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serviceName: TextView = itemView.findViewById(R.id.serviceNameText)
        val serviceValueText : TextView = itemView.findViewById(R.id.serviceValueText)
        val serviceDescription : TextView = itemView.findViewById(R.id.displayServiceDescription)
        //val image : ImageView = itemView.findViewById(R.id.displayOpportunityStatusImage)

        val moreImageView: ImageView = itemView.findViewById(R.id.more_image_view)
        //val editButton: Button = itemView.findViewById(R.id.task_edit_button)
        // val deleteButton: Button = itemView.findViewById(R.id.task_delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.service_item, parent, false)
        return ServiceViewHolder(view)
    }


    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        //sets all the services with their data
        val service = services[position]
        holder.serviceName.text = service.ProductName
        holder.serviceValueText.text = "R" + service.Price.toString()
        holder.serviceDescription.text = service.Description
        holder.moreImageView.setOnClickListener {
            showPopupMenu(holder.moreImageView, service)
        }
       // Picasso.get().load(products.Image).into(holder.image)

        /*holder.moreImageView.setOnClickListener {
            showPopupMenu(holder.moreImageView, opportunity)
        }*/
    }


    override fun getItemCount(): Int = services.size

    private fun showPopupMenu(view: View, service: ProductModel) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.menu_items, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit_item -> {
                    // Call the edit click listener
                    onEditClick(service)
                    true
                }
                R.id.delete_item -> {
                    // Call the delete click listener
                    onDeleteClick(service)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
    fun removeService(product: ProductModel) {
        val position = services.indexOfFirst { it.id == product.id }
        if (position != -1) {
            // Remove the services at the found position
            services.removeAt(position)  // Change this line to use 'opportunities' list
            // Notify that an item was removed
            notifyItemRemoved(position)
        }
    }
    fun updateProduct(updatedProduct: ProductModel) {
        val index = services.indexOfFirst { it.id == updatedProduct.id }
        if (index != -1) {
            services[index] = updatedProduct
            notifyItemChanged(index) // Notify the adapter of the item change
        }
    }


    fun updateServices(newProducts: List<ProductModel>) {
        val oldSize = services.size
        services.clear()
        services.addAll(newProducts)
        // Notify that all old items were removed
        notifyItemRangeRemoved(0, oldSize)
        // Notify that new items were added
        notifyItemRangeInserted(0, newProducts.size)
    }

    fun updateProducts(newService: List<ProductModel>) {
        Log.d("OpportunityAdapter", "Updating opportunities: ${newService.size} items")
        services = newService.toMutableList()
        notifyDataSetChanged()
    }
}