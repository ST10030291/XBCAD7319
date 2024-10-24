package com.example.xbcad7319_vucadigital.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.models.CustomerProductModel
import com.example.xbcad7319_vucadigital.models.TaskModel

class CustomerProductsAdapter(private val customerProducts: MutableList<CustomerProductModel> = mutableListOf()) :
    RecyclerView.Adapter<CustomerProductsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productNameTextView: TextView = view.findViewById(R.id.ProductName)
        val contractPeriodTextView: TextView = view.findViewById(R.id.ContractPeriod)
        val serviceProviderTextView: TextView = view.findViewById(R.id.ServiceProvider)
        val contractTermTextView: TextView = view.findViewById(R.id.ContractTerm)
        val statusTextView: TextView = view.findViewById(R.id.CPStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.customer_products_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val customer = customerProducts[position]

        holder.productNameTextView.text = customer.ProductName
        (customer.ContractStart + " - " + customer.ContractEnd).also { holder.contractPeriodTextView.text = it }
        holder.statusTextView.text = customer.Status
        holder.serviceProviderTextView.text = customer.ServiceProvider
        holder.contractTermTextView.text = customer.ContractTerm
    }

    fun updateCustomerProducts(newCustomerProducts: List<CustomerProductModel>) {
        val oldSize = customerProducts.size
        customerProducts.clear()
        customerProducts.addAll(newCustomerProducts)
        notifyItemRangeRemoved(0, oldSize)
        notifyItemRangeInserted(0, newCustomerProducts.size)
    }

    override fun getItemCount(): Int {
        return customerProducts.size
    }

}