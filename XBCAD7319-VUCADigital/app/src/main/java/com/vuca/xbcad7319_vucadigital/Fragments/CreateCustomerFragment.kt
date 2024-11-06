package com.vuca.xbcad7319_vucadigital.Fragments

import android.app.AlertDialog
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
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.vuca.xbcad7319_vucadigital.R
import com.vuca.xbcad7319_vucadigital.databinding.ActivityDashboardBinding
import com.vuca.xbcad7319_vucadigital.db.SupabaseHelper
import com.vuca.xbcad7319_vucadigital.models.CustomerModel
import kotlinx.coroutines.launch

class CreateCustomerFragment : Fragment() {
    private val sbHelper = SupabaseHelper()
    private lateinit var selectedItem: String
    private lateinit var customerTypeDropdown: AutoCompleteTextView
    private lateinit var binding: ActivityDashboardBinding
    private var isUpdateMode = false
    private var customer: CustomerModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_customer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)

        setupDropdown(view)
        handleArguments()
        setupUI(view)

        view.findViewById<ImageView>(R.id.back_btn).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupDropdown(view: View) {
        customerTypeDropdown = view.findViewById(R.id.autoCompleteText)
        val customerTypes = arrayOf("Prospect", "Leads", "Referrals")
        val adapter = ArrayAdapter(requireContext(), R.layout.customer_type_list_item, customerTypes)
        customerTypeDropdown.setAdapter(adapter)

        customerTypeDropdown.setOnItemClickListener { parent, _, position, _ ->
            selectedItem = parent.getItemAtPosition(position) as String
            customerTypeDropdown.hint = selectedItem
        }
    }

    private fun handleArguments() {
        arguments?.let {
            isUpdateMode = it.getBoolean("isUpdateMode", false)
            customer = it.getSerializable("customer") as? CustomerModel
        }
    }

    private fun setupUI(view: View) {
        val titleTextView = view.findViewById<TextView>(R.id.pageName)
        val actionButton = view.findViewById<Button>(R.id.createCustomerButton)

        if (isUpdateMode) {
            titleTextView.text = getString(R.string.update_customer)
            actionButton.text = getString(R.string.save)
            customer?.let { populateFields(it, view) }
        } else {
            titleTextView.text = getString(R.string.new_customer)
            actionButton.text = getString(R.string.create)
        }

        actionButton.setOnClickListener {
            if (isUpdateMode) updateCustomer(view) else createCustomer(view)
        }
    }

    private fun initializeFields(view: View): CustomerModel? {
        val customerName = getFieldText(view, R.id.customerName)
        val telephoneNumber = getFieldText(view, R.id.telephoneNumber)
        val customerEmail = getFieldText(view, R.id.customerEmail)
        val accountNumber = getFieldText(view, R.id.accountNumber)
        val billingAccountNumber = getFieldText(view, R.id.billingAccountNumber)

        if (validateFields(customerName, telephoneNumber, customerEmail, accountNumber, billingAccountNumber)) return null

        return CustomerModel(
            id = customer?.id.takeIf { isUpdateMode },
            CustomerName = customerName,
            TelephoneNumber = telephoneNumber,
            CustomerEmail = customerEmail,
            AccountNumber = accountNumber,
            BillingAccountNumber = billingAccountNumber,
            CustomerType = selectedItem,
            Products = customer?.Products.takeIf { isUpdateMode } ?: emptyList()
        )
    }

    private fun getFieldText(view: View, id: Int): String {
        return view.findViewById<EditText>(id).text.toString()
    }

    private fun validateFields(vararg fields: String): Boolean {
        fields.forEach { field ->
            if (field.isEmpty()) {
                errorDialog()
                return true
            }
        }
        return false
    }

    private fun errorDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Invalid Data")
            .setMessage("Please enter all values")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun populateFields(customer: CustomerModel, view: View) {
        view.findViewById<EditText>(R.id.customerName).setText(customer.CustomerName)
        view.findViewById<EditText>(R.id.customerEmail).setText(customer.CustomerEmail)
        view.findViewById<EditText>(R.id.telephoneNumber).setText(customer.TelephoneNumber)
        view.findViewById<EditText>(R.id.accountNumber).setText(customer.AccountNumber)
        view.findViewById<EditText>(R.id.billingAccountNumber).setText(customer.BillingAccountNumber)
        customerTypeDropdown.setText(customer.CustomerType, false)
        selectedItem = customer.CustomerType
    }

    private fun createCustomer(view: View) {
        lifecycleScope.launch {
            handleCustomerOperation(view, sbHelper::addCustomer, "Insert successfully", "Insertion failed!")
        }
    }

    private fun updateCustomer(view: View) {
        lifecycleScope.launch {
            handleCustomerOperation(view, sbHelper::updateCustomer, "Customer updated", "Update failed!")
        }
    }

    private suspend fun handleCustomerOperation(view: View, operation: suspend (CustomerModel) -> Boolean, successMessage: String, failureMessage: String) {
        try {
            val success = initializeFields(view)?.let { operation(it) }
            Toast.makeText(requireContext(), if (success == true) successMessage else failureMessage, Toast.LENGTH_SHORT).show()
            if (success == true) {
                sbHelper.updateAchievement("1e1af44d-7824-438e-af14-710e0a81d277",1)
                requireActivity().supportFragmentManager.popBackStack()
            }
        } catch (e: Exception) {
            Log.e("CreateCustomer", "Exception: ${e.message}")
            Toast.makeText(requireContext(), "Something went wrong! Operation cancelled.", Toast.LENGTH_SHORT).show()
        }
    }
}
