package com.example.xbcad7319_vucadigital.Fragments

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.compose.material3.DatePickerDialog
import androidx.lifecycle.lifecycleScope
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.db.SupabaseHelper
import com.example.xbcad7319_vucadigital.models.CustomerModel
import com.example.xbcad7319_vucadigital.models.OpportunityModel
import com.example.xbcad7319_vucadigital.models.TaskModel
import kotlinx.coroutines.launch
import java.util.Calendar

class CreateOpportunityFragment : Fragment() {
    
    private lateinit var date: EditText
    private lateinit var prioritySpinner: Spinner
    private lateinit var customerSpinner: Spinner
    private lateinit var statusSpinner: Spinner
    private lateinit var opportunityName: EditText
    private lateinit var value: EditText
    private lateinit var createButton: Button
    private lateinit var backButton: ImageView
    private lateinit var sbHelper: SupabaseHelper

    private lateinit var customer: CustomerModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout for current fragment
        val view = inflater.inflate(R.layout.fragment_create_opportunity, container, false)

        // Initialize UI
        opportunityName = view.findViewById(R.id.opportunityNameInput)
        value = view.findViewById(R.id.valueInput)
        customerSpinner = view.findViewById(R.id.customerNameInput)
        prioritySpinner = view.findViewById(R.id.priorityOfOpportunitySpinner)
        statusSpinner = view.findViewById(R.id.statusInput)
        date = view.findViewById(R.id.dateInput)
        createButton = view.findViewById(R.id.createOpportunityButton)
        backButton = view.findViewById(R.id.back_btn)


        customerDropDown()

        date.setOnClickListener {
            showDatePickerDialog { selectedDate ->
                date.setText(selectedDate)
            }
        }

        createButton.setOnClickListener {
            createOpportunity()
        }
        backButton.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }
        return view
    }

    private fun createOpportunity() {
        // Retrieve values from the inputs
        val opportunityName = opportunityName.text.toString()
        val valueString = value.text.toString()
        val value: Double = valueString.toDouble()
        val customerSpinner = customerSpinner.selectedItem.toString()
        val prioritySpinner = prioritySpinner.selectedItem.toString()
        val statusSpinner = statusSpinner.selectedItem.toString()
        val date = date.text.toString()


        // Validate inputs
        if (!validateInputs(opportunityName, value, customerSpinner, prioritySpinner, statusSpinner, date)) return


        val opportunity = OpportunityModel(
            OpportunityName = opportunityName,
            TotalValue = value,
            Stage = "1",
            CustomerName = customerSpinner,
            Priority = prioritySpinner,
            Status = statusSpinner,
            CreationDate = date
        )


        Log.d(opportunity.OpportunityName, "${opportunity.OpportunityName} saved successfully!")
        Toast.makeText(requireContext(), "Task created successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun validateInputs(
        opportunityName: String,
        value: Double,
        customerSpinner: String,
        prioritySpinner: String,
        statusSpinner: String,
        date: String
    ): Boolean {
        return when {
            opportunityName.isEmpty() -> {
                showToast("Empty Opportunity Name! Please enter a opportunity name.")
                false
            }
            value == null -> {
                showToast("Please enter a value.")
                false
            }
            customerSpinner == "Select a Customer" -> {
                showToast("Customer not selected! Please select a customer.")
                false
            }
            statusSpinner == "Select Status" -> {
                showToast("Status Level assigned not selected! Please select a status level.")
                false
            }
            prioritySpinner == "Select Priority" -> {
                showToast("Priority level not selected! Please select a priority level.")
                false
            }
            date.isEmpty() -> {
                showToast("Empty date! Please enter a Date.")
                false
            }
            // If we're here, everything was okay
            else -> true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


    private fun customerDropDown(){
        lifecycleScope.launch {
            try {
                val customerNames = sbHelper.getAllCustomersNames()

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, customerNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                customerSpinner.adapter = adapter
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format selected date as "YYYY/MM/DD"
                val selectedDate = String.format("%04d/%02d/%02d", selectedYear, selectedMonth + 1, selectedDay)

                onDateSelected(selectedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
}