package com.example.xbcad7319_vucadigital.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.models.ProductModel
import com.squareup.picasso.Picasso

class ServiceAdapter  (private var services: List<ProductModel>/*,
                          private val onEditClick: (ProductModel) -> Unit,
                          private val onDeleteClick: (ProductModel) -> Unit*/
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
        val service = services[position]
        holder.serviceName.text = service.ProductName
        holder.serviceValueText.text = "R" + service.Price.toString()
        holder.serviceDescription.text = service.Description

       // Picasso.get().load(products.Image).into(holder.image)

        /*holder.moreImageView.setOnClickListener {
            showPopupMenu(holder.moreImageView, opportunity)
        }*/
    }


    override fun getItemCount(): Int = services.size

    fun updateProducts(newService: List<ProductModel>) {
        Log.d("OpportunityAdapter", "Updating opportunities: ${newService.size} items")
        services = newService
        notifyDataSetChanged()
    }
}