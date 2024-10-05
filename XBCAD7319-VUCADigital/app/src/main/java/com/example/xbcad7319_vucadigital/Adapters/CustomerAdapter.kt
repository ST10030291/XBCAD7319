package com.example.xbcad7319_vucadigital.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.models.CustomerModel

class CustomerAdapter(context: Context, customers: List<CustomerModel>) :
    ArrayAdapter<CustomerModel>(context, 0, customers) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val customers = getItem(position)
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.customer_grid_item, parent, false)

            val name: TextView = view.findViewById(R.id.CustomerName)
            val type: TextView = view.findViewById(R.id.CustomerType)

            name.text = customers?.CustomerName
            type.text = customers?.CustomerType

            return view
        }
}