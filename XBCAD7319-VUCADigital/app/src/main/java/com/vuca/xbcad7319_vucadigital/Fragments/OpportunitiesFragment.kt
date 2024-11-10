package com.vuca.xbcad7319_vucadigital.Fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vuca.xbcad7319_vucadigital.Activites.DashboardActivity
import com.vuca.xbcad7319_vucadigital.Adapters.CustomSpinnerAdapter
import com.vuca.xbcad7319_vucadigital.Adapters.OpportunityAdapter
import com.vuca.xbcad7319_vucadigital.R
import com.vuca.xbcad7319_vucadigital.db.SupabaseHelper
import com.vuca.xbcad7319_vucadigital.models.CustomerModel
import com.vuca.xbcad7319_vucadigital.models.OpportunityModel
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class OpportunitiesFragment : Fragment() {
    //globally declaring all variables
    private lateinit var opportunityAdapter: OpportunityAdapter
    private var opportunityList = mutableListOf<OpportunityModel>()
    private lateinit var customers: List<CustomerModel>
    private lateinit var sbHelper: SupabaseHelper


    private lateinit var filteredOpportunities: List<OpportunityModel>
    private lateinit var opportunitiesSearch: List<OpportunityModel>
    private lateinit var searchView: SearchView

    override fun onResume() {
        super.onResume()
        val dashboardActivity = activity as? DashboardActivity
        dashboardActivity?.binding?.apply {
            bottomNavigation.visibility = View.VISIBLE
            plusBtn.visibility = View.VISIBLE
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_opportunities, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchView = view.findViewById(R.id.opportunitySearchView)
        val recyclerView: RecyclerView = view.findViewById(R.id.opportunity_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        opportunityAdapter = OpportunityAdapter(opportunityList, ::onEditOpportunity, ::onDeleteOpportunity)//calling the adapter
        recyclerView.adapter = opportunityAdapter//setting all the opportunities to match the adapter

        sbHelper = SupabaseHelper()

        Handler(Looper.getMainLooper()).postDelayed({
            val shimmerLayout = view.findViewById<ShimmerFrameLayout>(R.id.shimmerOpportunities)
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            loadOpportunity()

            setUpSearchView()
        },2000)

    }
    private fun setUpSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            //takes in user input
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchOpportunitiesByName(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchOpportunitiesByName(it) }
                return true
            }
        })
    }

    private fun searchOpportunitiesByName(query: String) {
        val queryLower = query.lowercase()

        filteredOpportunities = opportunitiesSearch.filter { opportunity ->
            opportunity.CustomerName.lowercase().contains(queryLower)
        }

        opportunityAdapter.updateOpportunities(filteredOpportunities)

        if (filteredOpportunities.isEmpty()) {
            Toast.makeText(context, "Opportunity \"$query\" not found!", Toast.LENGTH_SHORT).show()
        }
    }


    private fun loadOpportunity() {
        lifecycleScope.launch {
            try {
                val opportunities = sbHelper.getAllOpportunities()
                opportunitiesSearch = sbHelper.getAllOpportunities()//gets all the opportunities
                Log.d("Opportunities", "Number of opportunities retrieved: ${opportunities.size}")
                opportunityAdapter.updateOpportunity(opportunities)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Couldn't load tasks from DB", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun onDeleteOpportunity(opportunity: OpportunityModel) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_task, null)
        val deleteDialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        val taskNameTextView: TextView = dialogView.findViewById(R.id.dialog_task_name)
        val messageTextView: TextView = dialogView.findViewById(R.id.dialog_message)
        val cancelButton: Button = dialogView.findViewById(R.id.button_cancel)
        val deleteButton: Button = dialogView.findViewById(R.id.button_delete)

        messageTextView.text = "Are you sure you want to delete this opportunity?"

        // Set the task name in the dialog
        taskNameTextView.text = opportunity.OpportunityName

        cancelButton.setOnClickListener {
            deleteDialog.dismiss()
            Toast.makeText(requireContext(), "Operation cancelled! No opportunity deleted.", Toast.LENGTH_SHORT).show()
        }

        deleteButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Delete the task from the database
                    sbHelper.deleteOpportunity(opportunity.id!!)//deletes the opportunity
                    withContext(Dispatchers.Main) {
                        opportunityAdapter.removeOpportunity(opportunity)
                        Toast.makeText(requireContext(), "Operation Success! Opportunity deleted.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("TaskDeleteError", "Error deleting task: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Opportunity failure! Couldn't delete opportunity.", Toast.LENGTH_SHORT).show()
                    }
                }
                deleteDialog.dismiss()
            }
        }

        deleteDialog.show()
    }

    private fun onEditOpportunity(opportunity: OpportunityModel) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_opportunity, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Edit Opportunity")

        setupDialogViews(dialogView, opportunity)

        val dialog = builder.create()

        setupDatePickers(dialogView, opportunity)

        setupDialogButtons(dialog, dialogView, opportunity)

        dialog.show()
    }
    private fun setupDatePickers(dialogView: View, opportunity: OpportunityModel) {
        val dateEditText: EditText = dialogView.findViewById(R.id.dateInput)

        // Set initial values
        dateEditText.setText(opportunity.CreationDate.replace("-", "/"))

        // Show date picker when start date is clicked
        dateEditText.setOnClickListener {
            showDatePickerDialog(dateEditText, opportunity.CreationDate)
        }
    }
    private fun setupDialogButtons(dialog: AlertDialog, dialogView: View, opportunity: OpportunityModel) {
        dialogView.findViewById<Button>(R.id.cancelEditOpportunity).setOnClickListener {
            dialog.dismiss() // Close the dialog
        }

        dialogView.findViewById<Button>(R.id.saveEditOpportunity).setOnClickListener {
            saveOpportunityChanges(dialog, opportunity, dialogView)
        }
    }
    private fun setupDialogViews(dialogView: View, opportunity: OpportunityModel) {
        // Get references to the dialog views
        val opportunityNameEditText: EditText = dialogView.findViewById(R.id.opportunityNameInput)
        val stageSpinner: Spinner = dialogView.findViewById(R.id.stageInput)
        val customerSpinner: Spinner = dialogView.findViewById(R.id.customerNameInput)
        val statusSpinner: Spinner = dialogView.findViewById(R.id.statusInput)
        val prioritySpinner: Spinner = dialogView.findViewById(R.id.priorityOfOpportunitySpinner)
        val valueEditText: EditText = dialogView.findViewById(R.id.valueInput)
        val dateEditText: EditText = dialogView.findViewById(R.id.dateInput)

        // Populate customer spinner
        lifecycleScope.launch {
            try {
                // Retrieve customer names and populate spinner
                customers = sbHelper.getAllCustomers()
                val customerNames = listOf("Select a customer name") + customers.map { it.CustomerName }
                customerSpinner.adapter = CustomSpinnerAdapter(requireContext(), customerNames)

                // Set selection to a specific name
                opportunity.CustomerName.let { customerName ->
                    val customerIndex = customerNames.indexOf(customerName)
                    if (customerIndex != -1) {
                        customerSpinner.setSelection(customerIndex)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Operation failure! Couldn't load customers.", Toast.LENGTH_SHORT).show()
            }
        }

        val leadStatusOptions = resources.getStringArray(R.array.leadStatusOptions)
        val priorityOptions = resources.getStringArray(R.array.priorityOptions)
        val stageOptions = resources.getStringArray(R.array.stageOptions)
        // Populate fields with the task's current values
        opportunityNameEditText.setText(opportunity.OpportunityName)
        stageSpinner.setSelection(stageOptions.indexOf(opportunity.Stage))

        prioritySpinner.setSelection(priorityOptions.indexOf(opportunity.Priority))
        statusSpinner.setSelection(leadStatusOptions.indexOf(opportunity.Status))
        valueEditText.setText(opportunity.TotalValue.toString())
        dateEditText.setText(opportunity.CreationDate)
    }
    private fun saveOpportunityChanges(dialog: AlertDialog, opportunity: OpportunityModel, dialogView: View) {
        val opportunityNameEditText: EditText = dialogView.findViewById(R.id.opportunityNameInput)
        val stageSpinner: Spinner = dialogView.findViewById(R.id.stageInput)
        val customerSpinner: Spinner = dialogView.findViewById(R.id.customerNameInput)
        val statusSpinner: Spinner = dialogView.findViewById(R.id.statusInput)
        val prioritySpinner: Spinner = dialogView.findViewById(R.id.priorityOfOpportunitySpinner)
        val valueEditText: EditText = dialogView.findViewById(R.id.valueInput)
        val valueString = valueEditText.text.toString()
        val value: Double = valueString.toDouble()
        val dateEditText: EditText = dialogView.findViewById(R.id.dateInput)

        lifecycleScope.launch {
            val updatedOpportunity = opportunity.copy(
                OpportunityName = opportunityNameEditText.text.toString(),
                TotalValue = value,
                Stage = stageSpinner.selectedItem.toString(),
                CustomerName = customerSpinner.selectedItem.toString(),
                Priority = prioritySpinner.selectedItem.toString(),
                Status = statusSpinner.selectedItem.toString(),
                CreationDate = dateEditText.text.toString()
            )

            try {
                // Update the task in the database
                sbHelper.updateOpportunity(updatedOpportunity)

                // Update the task in the adapter
                opportunityAdapter.updateOpportunity(updatedOpportunity)

                Toast.makeText(requireContext(), "Operation Success! Updated opportunity.", Toast.LENGTH_SHORT).show()
                dialog.dismiss() // Dismiss only after success
            } catch (e: Exception) {
                Log.e("EditTask", "Error updating opportunity", e) // Log error details
                Toast.makeText(requireContext(), "Operation failure! Couldn't update opportunity.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePickerDialog(editText: EditText, initialDate: String) {
        val calendar = Calendar.getInstance()

        if (initialDate.isNotEmpty()) {
            val dateParts = initialDate.split("-")
            calendar.set(dateParts[0].toInt(), dateParts[1].toInt() - 1, dateParts[2].toInt())
        }

        // Create the DatePickerDialog with custom theme
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format(Locale.getDefault(),"%04d/%02d/%02d", year, month + 1, dayOfMonth)
                editText.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Show the DatePickerDialog
        datePickerDialog.show()
    }

}