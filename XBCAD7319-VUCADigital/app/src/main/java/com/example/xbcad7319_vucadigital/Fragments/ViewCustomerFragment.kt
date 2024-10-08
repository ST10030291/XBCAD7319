package com.example.xbcad7319_vucadigital.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.xbcad7319_vucadigital.Adapters.CustomerAdapter
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.db.SupabaseHelper
import com.example.xbcad7319_vucadigital.models.CustomerModel
import kotlinx.coroutines.launch

class ViewCustomerFragment : Fragment() {
    private lateinit var customer: CustomerModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            customer = it.getSerializable("customer") as CustomerModel
        }

        displayCustomerData(view)

        val callButton: ImageView = view.findViewById(R.id.customer_call_click)
        callButton.setOnClickListener {
            makePhoneCall(customer.TelephoneNumber)
        }
    }

    private fun makePhoneCall(phoneNumber: String) {
        if (phoneNumber.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
        } else {
            Toast.makeText(context, "Phone number is not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayCustomerData(view: View) {
        val customerNameTextView: TextView = view.findViewById(R.id.customer_name)
        val emailTextView: TextView = view.findViewById(R.id.customer_email)
        val customerType: TextView = view.findViewById(R.id.customer_type)
        val accountNumberTextView: TextView = view.findViewById(R.id.account_number)
        val billingAccountNumberTextView: TextView = view.findViewById(R.id.billing_account_number)

        customerNameTextView.text = customer.CustomerName
        emailTextView.text = customer.CustomerEmail
        customerType.text = customer.CustomerType
        billingAccountNumberTextView.text = customer.BillingAccountNumber
        accountNumberTextView.text = customer.AccountNumber
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_customer, container, false)
    }


}