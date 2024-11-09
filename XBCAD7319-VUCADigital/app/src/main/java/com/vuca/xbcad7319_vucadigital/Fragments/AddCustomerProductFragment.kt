package com.vuca.xbcad7319_vucadigital.Fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.vuca.xbcad7319_vucadigital.R
import com.vuca.xbcad7319_vucadigital.db.SupabaseHelper
import com.vuca.xbcad7319_vucadigital.models.CustomerModel
import com.vuca.xbcad7319_vucadigital.models.CustomerProductModel
import com.vuca.xbcad7319_vucadigital.models.ProductModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddCustomerProductFragment : Fragment() {

    private lateinit var customerNameInput: AutoCompleteTextView
    private lateinit var productNameInput: AutoCompleteTextView
    private lateinit var contractStartDateInput: EditText
    private lateinit var contractEndDateInput: EditText
    private lateinit var contractTermInput: EditText
    private lateinit var serviceProviderInput: EditText
    private lateinit var createCustomerProductButton: Button
    private lateinit var statusSpinner: Spinner
    private lateinit var sbHelper: SupabaseHelper
    private lateinit var customers: List<CustomerModel>
    private lateinit var products: List<ProductModel>
    private lateinit var customerAdapter: ArrayAdapter<String>
    private lateinit var productAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_customer_product, container, false)

        // Initialize UI elements
        customerNameInput = view.findViewById(R.id.customerNameInput)
        productNameInput = view.findViewById(R.id.productNameInput)
        contractStartDateInput = view.findViewById(R.id.contractStartDateInput)
        contractEndDateInput = view.findViewById(R.id.contractEndDateInput)
        contractTermInput = view.findViewById(R.id.contractTermInput)
        serviceProviderInput = view.findViewById(R.id.serviceProviderInput)
        createCustomerProductButton = view.findViewById(R.id.createButton)
        statusSpinner = view.findViewById(R.id.statusSpinner)

        // Initialize DB helper
        sbHelper = SupabaseHelper()

        lifecycleScope.launch {
            customers = sbHelper.getAllCustomers()
            products = sbHelper.getAllProducts()
            setupCustomerAutoComplete()
            setupProductAutoComplete()
        }

        // Setup status spinner
        setupStatusSpinner()

        val backButton: ImageView = view.findViewById(R.id.back_btn)
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Set up date pickers
        contractStartDateInput.setOnClickListener {
            showDatePickerDialog { selectedDate ->
                contractStartDateInput.setText(selectedDate)
            }
        }

        contractEndDateInput.setOnClickListener {
            showDatePickerDialog { selectedDate ->
                contractEndDateInput.setText(selectedDate)
            }
        }

        // Set up create button listener
        createCustomerProductButton.setOnClickListener {
            if (validateInputs()) {
                createCustomerProduct()
                Log.d("Create Button Clicked", "Method called")
            }
            else{
                Log.d("Create Button Clicked", "Method not called")
            }
        }

        return view
    }

    private fun setupCustomerAutoComplete() {
        val customerNames = customers.map { it.CustomerName }
        customerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, customerNames)
        customerNameInput.setAdapter(customerAdapter)
        customerNameInput.threshold = 1
    }

    private fun setupProductAutoComplete() {
        val productNames = products.map { it.ProductName }
        productAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, productNames)
        productNameInput.setAdapter(productAdapter)
        productNameInput.threshold = 1
    }

    private fun setupStatusSpinner() {
        val statusOptions = arrayOf(
            "Select status",
            "Active",
            "Inactive",
            "Trial",
            "Contract Pending",
            "Renewal Due",
            "Suspended"
        )
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statusSpinner.adapter = adapter
    }

    private fun showDatePickerDialog(onDateSet: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = "$dayOfMonth/${month + 1}/$year"
                onDateSet(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun validateInputs(): Boolean {
        val customerName = customerNameInput.toString()
        val productName = productNameInput.toString()
        val contractStartDate = contractStartDateInput.text.toString().trim()
        val contractEndDate = contractEndDateInput.text.toString().trim()
        val contractTerm = contractTermInput.text.toString().trim()
        val serviceProvider = serviceProviderInput.text.toString().trim()
        val statusSelected = statusSpinner.selectedItem

        return when {
            customerName.isEmpty() -> {
                Toast.makeText(requireContext(), "Please enter customer name", Toast.LENGTH_SHORT)
                    .show()
                false
            }

            productName.isEmpty() -> {
                Toast.makeText(requireContext(), "Please enter product name", Toast.LENGTH_SHORT)
                    .show()
                false
            }

            contractStartDate.isEmpty() -> {
                Toast.makeText(
                    requireContext(),
                    "Please enter contract start date",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }

            contractEndDate.isEmpty() -> {
                Toast.makeText(
                    requireContext(),
                    "Please enter contract end date",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }

            contractTerm.isEmpty() -> {
                Toast.makeText(requireContext(), "Please enter contract term", Toast.LENGTH_SHORT)
                    .show()
                false
            }

            serviceProvider.isEmpty() -> {
                Toast.makeText(
                    requireContext(),
                    "Please enter service provider",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }

            statusSelected == null || statusSelected == "Select status" -> {
                Toast.makeText(requireContext(), "Please select a status", Toast.LENGTH_SHORT)
                    .show()
                false
            }

            else -> true
        }
    }

    private fun createCustomerProduct() {
        val customerName = customerNameInput.text.toString()
        val productName = productNameInput.text.toString()
        val contractStartDate = formatDate(contractStartDateInput.text.toString().trim())
        val contractEndDate = formatDate(contractEndDateInput.text.toString().trim())
        val contractTerm = contractTermInput.text.toString().trim()
        val serviceProvider = serviceProviderInput.text.toString().trim()
        val status = statusSpinner.selectedItem.toString().trim()

        // Create CustomerProductModel object
        val newProduct = CustomerProductModel(
            CustomerName = customerName,
            ProductName = productName,
            ContractStart = contractStartDate ?: "",
            ContractEnd = contractEndDate ?: "",
            ContractTerm = contractTerm,
            ServiceProvider = serviceProvider,
            Status = status
        )

        // Save to database using coroutine
        lifecycleScope.launch {
            try {
                val success = sbHelper.addCustomerProduct(newProduct)
                if (success) {
                    Toast.makeText(requireContext(), "Product added successfully!", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                    clearInputs()
                } else {
                    Log.e("AddCustomerProduct", "Failed to add product")
                    Toast.makeText(requireContext(), "Failed to add product", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("AddCustomerProduct", "An error occurred", e)
                Toast.makeText(requireContext(), "An error occurred. Please check logs.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearInputs() {
        customerNameInput.text.clear()
        productNameInput.text.clear()
        contractStartDateInput.text.clear()
        contractEndDateInput.text.clear()
        contractTermInput.text.clear()
        serviceProviderInput.text.clear()
        statusSpinner.setSelection(0)
    }

    private fun formatDate(dateString: String): String? {
        return try {
            // Define the input and output date formats
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            Log.e("DateFormatError", "Invalid date format", e)
            null
        }
    }
}