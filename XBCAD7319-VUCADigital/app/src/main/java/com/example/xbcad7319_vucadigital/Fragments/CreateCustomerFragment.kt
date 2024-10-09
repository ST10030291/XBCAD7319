package com.example.xbcad7319_vucadigital.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.xbcad7319_vucadigital.Activites.DashboardActivity
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.databinding.ActivityDashboardBinding
import com.example.xbcad7319_vucadigital.db.SupabaseHelper
import com.example.xbcad7319_vucadigital.models.CustomerModel
import com.example.xbcad7319_vucadigital.models.CustomerProduct
import kotlinx.coroutines.launch

class CreateCustomerFragment : Fragment() {
    private val sbHelper = SupabaseHelper()
    private lateinit var selectedItem: String
    private lateinit var binding: ActivityDashboardBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)

        val createBtn = view.findViewById<Button>(R.id.createCustomerButton)
        val customerTypes = arrayOf("Prospect", "Leads", "Referrals")
        val customerTypeDropdown: AutoCompleteTextView = view.findViewById(R.id.autoCompleteText)
        val adapter = ArrayAdapter(requireContext(), R.layout.customer_type_list_item, customerTypes)
        val backBtn = view.findViewById<ImageView>(R.id.back_btn)

        customerTypeDropdown.setAdapter(adapter)

        customerTypeDropdown.setOnItemClickListener { parent, view, position, id ->
            selectedItem = parent.getItemAtPosition(position) as String
            customerTypeDropdown.hint = selectedItem

        }

        backBtn.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        createBtn.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val success = sbHelper.addCustomer(initializeFields(view))

                    if (success) {
                        Toast.makeText(requireContext(), "Insert successfully", Toast.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Insertion failed!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("CreateCustomer", "Exception: ${e.message}")
                    Toast.makeText(requireContext(), "Something went wrong! Insert process cancelled.", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_customer, container, false)
    }

    private fun initializeFields(view: View): CustomerModel {
        val customerName = view.findViewById<EditText>(R.id.customerName).text.toString()
        val telephoneNumber = view.findViewById<EditText>(R.id.telephoneNumber).text.toString()
        val customerEmail = view.findViewById<EditText>(R.id.customerEmail).text.toString()
        val accountNumber = view.findViewById<EditText>(R.id.accountNumber).text.toString()
        val billingAccountNumebr = view.findViewById<EditText>(R.id.billingAccountNumber).text.toString()
        val customerType = selectedItem


        return CustomerModel(id=null,customerName, telephoneNumber, customerEmail,accountNumber, billingAccountNumebr, customerType ,
            emptyList()
        )
    }

}