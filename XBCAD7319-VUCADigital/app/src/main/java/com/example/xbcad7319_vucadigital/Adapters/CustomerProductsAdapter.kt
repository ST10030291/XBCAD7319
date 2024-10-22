package com.example.xbcad7319_vucadigital.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.xbcad7319_vucadigital.Adapters.CustomerAdapter.ViewHolder
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.models.CustomerModel
import com.example.xbcad7319_vucadigital.models.CustomerProductModel

class CustomerProductsAdapter(context: Context, private val customerProductModels: List<CustomerProductModel>) :
    ArrayAdapter<CustomerProductModel>(context, 0, customerProductModels) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: CustomerProductsAdapter.ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.customer_products_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as CustomerProductsAdapter.ViewHolder
        }

        val customer = getItem(position)

        customer?.let {
            viewHolder.productNameTextView.text = customer.ProductName
            (customer.ContractStart + " - " + customer.ContractEnd).also { viewHolder.contractPeriodTextView.text = it }
            viewHolder.statusTextView.text = customer.Status
            viewHolder.serviceProviderTextView.text = customer.ServiceProvider
            viewHolder.contractTermTextView.text = customer.ContractTerm
        }

        return view
    }

    inner class ViewHolder(view: View) {
        val productNameTextView: TextView = view.findViewById(R.id.CustomerName)
        val contractPeriodTextView: TextView = view.findViewById(R.id.ContractPeriod)
        val serviceProviderTextView: TextView = view.findViewById(R.id.ServiceProvider)
        val contractTermTextView: TextView = view.findViewById(R.id.ContractTerm)
        val statusTextView: TextView = view.findViewById(R.id.CPStatus)
    }
}