package com.example.xbcad7319_vucadigital.Fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import kotlinx.coroutines.launch

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
        opportunityAdapter = OpportunityAdapter(opportunityList)
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
    /*private fun setupDialogButtons(dialog: AlertDialog, dialogView: View, task: TaskModel) {
        dialogView.findViewById<Button>(R.id.cancelEditTask).setOnClickListener {
            dialog.dismiss() // Close the dialog
        }

        dialogView.findViewById<Button>(R.id.saveEditTask).setOnClickListener {
            saveTaskChanges(dialog, task, dialogView)
        }
    }*/

}