package com.example.xbcad7319_vucadigital.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.models.CustomerModel

class CustomerAdapter(context: Context, private val customers: List<CustomerModel>) :
    ArrayAdapter<CustomerModel>(context, 0, customers) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.customer_grid_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val customer = getItem(position)

        customer?.let {
            viewHolder.nameTextView.text = customer.CustomerName
            viewHolder.typeTextView.text = customer.CustomerType
        }

        return view
    }

    private class ViewHolder(view: View) {
        val nameTextView: TextView = view.findViewById(R.id.CustomerName)
        val typeTextView: TextView = view.findViewById(R.id.CustomerType)
    }
}
