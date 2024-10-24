package com.example.xbcad7319_vucadigital.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xbcad7319_vucadigital.Activites.DashboardActivity
import com.example.xbcad7319_vucadigital.Adapters.CustomerProductsAdapter
import com.example.xbcad7319_vucadigital.Adapters.TaskAdapter
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.db.SupabaseHelper
import com.example.xbcad7319_vucadigital.models.CustomerModel
import com.example.xbcad7319_vucadigital.models.CustomerProductModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewCustomerFragment : Fragment() {
    private lateinit var customer: CustomerModel
    private lateinit var sbHelper: SupabaseHelper
    private var customerProducts = mutableListOf<CustomerProductModel>()
    private lateinit var customerProductAdapter: CustomerProductsAdapter

    override fun onResume() {
        super.onResume()
        val dashboardActivity = activity as? DashboardActivity
        dashboardActivity?.binding?.apply {
            plusBtn.visibility = View.GONE
            bottomNavigation.visibility = View.VISIBLE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sbHelper = SupabaseHelper()
        val recyclerView: RecyclerView = view.findViewById(R.id.customer_products_recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        customerProductAdapter = CustomerProductsAdapter(customerProducts)
        recyclerView.adapter = customerProductAdapter

        retrieveArguments()
        displayCustomerData(view)
        setupClickListeners(view)

        loadCustomerProducts()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_customer, container, false)
    }

    private fun retrieveArguments() {
        arguments?.let {
            customer = it.getSerializable("customer") as CustomerModel
        }
    }

    private fun displayCustomerData(view: View) {
        view.findViewById<TextView>(R.id.customer_name).text = customer.CustomerName
        view.findViewById<TextView>(R.id.customer_email).text = customer.CustomerEmail
        view.findViewById<TextView>(R.id.customer_type).text = customer.CustomerType
        view.findViewById<TextView>(R.id.account_number).text = customer.AccountNumber
        view.findViewById<TextView>(R.id.billing_account_number).text = customer.BillingAccountNumber
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<ImageView>(R.id.customer_call_click).setOnClickListener {
            makePhoneCall(customer.TelephoneNumber)
        }

        view.findViewById<ImageView>(R.id.customerDeleteBtn).setOnClickListener {
            lifecycleScope.launch { deleteCustomer() }
        }

        view.findViewById<ImageView>(R.id.customerUpdateBtn).setOnClickListener {
            navigateToUpdateCustomer()
        }

        view.findViewById<ImageView>(R.id.back_btn).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        view.findViewById<AppCompatButton>(R.id.NewCustomerProductBtn).setOnClickListener{
            navigateToCreateCustomerProductFragment()
        }
    }

    private fun makePhoneCall(phoneNumber: String) {
        if (phoneNumber.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            startActivity(intent)
        } else {
            Toast.makeText(context, "Phone number is not available", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun deleteCustomer() {
        try {
            customer.id?.let {
                sbHelper.deleteCustomer(it)
                showToast("Deletion successfully")
                popBackStack()
            }
        } catch (e: Exception) {
            showToast("Something went wrong! Deleting process cancelled.")
        }
    }

    private fun navigateToUpdateCustomer() {
        val updateCustomerFragment = CreateCustomerFragment().apply {
            arguments = Bundle().apply {
                putBoolean("isUpdateMode", true)
                putSerializable("customer", customer)
            }
        }

        (activity as? DashboardActivity)?.apply {
            binding.bottomNavigation.visibility = View.GONE
            binding.plusBtn.visibility = View.GONE
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, updateCustomerFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToCreateCustomerProductFragment() {

        (activity as? DashboardActivity)?.apply {
            binding.bottomNavigation.visibility = View.GONE
            binding.plusBtn.visibility = View.GONE
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AddCustomerProductFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun loadCustomerProducts() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                customer.CustomerName.let {
                    customerProducts = sbHelper.getCustomerProducts(it).toMutableList()
                }

                withContext(Dispatchers.Main) {
                    customerProductAdapter.updateCustomerProducts(customerProducts)
                }
            } catch (e: Exception) {
                Log.e("CustomerProductsLoadError", "Error loading customer products", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Couldn't load customer products from DB", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun popBackStack() {
        requireActivity().supportFragmentManager.popBackStack()
    }
}
