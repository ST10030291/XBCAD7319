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
import com.example.xbcad7319_vucadigital.Activites.DashboardActivity
import com.example.xbcad7319_vucadigital.Adapters.CustomerAdapter
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.db.SupabaseHelper
import com.example.xbcad7319_vucadigital.models.CustomerModel
import kotlinx.coroutines.launch

class ViewCustomerFragment : Fragment() {
    private lateinit var customer: CustomerModel
    private lateinit var sbHelper: SupabaseHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val callButton: ImageView = view.findViewById(R.id.customer_call_click)
        val DeleteBtn : ImageView = view.findViewById(R.id.customerDeleteBtn)
        val UpdateBtn : ImageView = view.findViewById(R.id.customerUpdateBtn)
        val backBtn : ImageView = view.findViewById(R.id.back_btn)

        sbHelper = SupabaseHelper()

        arguments?.let {
            customer = it.getSerializable("customer") as CustomerModel
        }

        displayCustomerData(view)

        callButton.setOnClickListener {
            makePhoneCall(customer.TelephoneNumber)
        }

        DeleteBtn.setOnClickListener{
            lifecycleScope.launch {
                deleteCustomer()
            }

        }

        UpdateBtn.setOnClickListener {
            val updateCustomerFragment = CreateCustomerFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isUpdateMode", true)
                    putSerializable("customer", customer)
                }
            }

            (activity as? DashboardActivity)?.let { dashboardActivity ->
                dashboardActivity.binding.bottomNavigation.visibility = View.GONE
                dashboardActivity.binding.plusBtn.visibility = View.GONE
            }

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, updateCustomerFragment)
                .addToBackStack(null)
                .commit()
        }


        backBtn.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
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

    private suspend fun deleteCustomer() {
        try {
            lifecycleScope.launch {
                customer.id?.let { sbHelper.deleteCustomer(it) }
                BacktoCustomers()
            }
            Toast.makeText(requireContext(), "Deletion successfully", Toast.LENGTH_SHORT).show()
        }
        catch (e: Exception){
            Toast.makeText(requireContext(), "Something went wrong! Deleting process cancelled.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun BacktoCustomers(){
        requireActivity().supportFragmentManager.popBackStack()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_customer, container, false)
    }


}