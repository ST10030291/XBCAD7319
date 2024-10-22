package com.example.xbcad7319_vucadigital.Fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xbcad7319_vucadigital.Activites.DashboardActivity
import com.example.xbcad7319_vucadigital.Adapters.OpportunityAdapter
import com.example.xbcad7319_vucadigital.Adapters.TaskAdapter
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.db.SupabaseHelper
import com.example.xbcad7319_vucadigital.models.OpportunityModel
import com.example.xbcad7319_vucadigital.models.TaskModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OpportunitiesFragment : Fragment() {
    private lateinit var opportunityAdapter: OpportunityAdapter
    private var opportunityList = mutableListOf<OpportunityModel>()
    private lateinit var sbHelper: SupabaseHelper
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

        val recyclerView: RecyclerView = view.findViewById(R.id.opportunity_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        opportunityAdapter = OpportunityAdapter(opportunityList, ::onDeleteOpportunity)
        recyclerView.adapter = opportunityAdapter

        sbHelper = SupabaseHelper()
        loadOpportunity()
    }

    private fun loadOpportunity() {
        lifecycleScope.launch {
            try {
                val opportunities = sbHelper.getAllOpportunities()
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
                    sbHelper.deleteOpportunity(opportunity.id!!)
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

    /*private fun setupDialogButtons(dialog: AlertDialog, dialogView: View, task: TaskModel) {
        dialogView.findViewById<Button>(R.id.cancelEditTask).setOnClickListener {
            dialog.dismiss() // Close the dialog
        }

        dialogView.findViewById<Button>(R.id.saveEditTask).setOnClickListener {
            saveTaskChanges(dialog, task, dialogView)
        }
    }*/

}