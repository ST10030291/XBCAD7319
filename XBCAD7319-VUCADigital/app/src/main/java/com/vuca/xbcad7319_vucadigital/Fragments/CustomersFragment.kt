package com.vuca.xbcad7319_vucadigital.Fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.vuca.xbcad7319_vucadigital.Activites.DashboardActivity
import com.vuca.xbcad7319_vucadigital.Adapters.CustomerAdapter
import com.vuca.xbcad7319_vucadigital.R
import com.vuca.xbcad7319_vucadigital.db.SupabaseHelper
import com.vuca.xbcad7319_vucadigital.models.CustomerModel
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.launch

class CustomersFragment : Fragment() {

    private lateinit var sbHelper: SupabaseHelper
    private lateinit var customers: List<CustomerModel>
    private lateinit var filteredCustomers: List<CustomerModel>
    private lateinit var allFilterButton: Button
    private lateinit var prospectFilterButton: Button
    private lateinit var leadsFilterButton: Button
    private lateinit var referralsFilterButton: Button
    private lateinit var searchView: SearchView
    private lateinit var gridView: GridView

    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var notFoundLayout: LinearLayout
    private lateinit var notFoundImg: ImageView
    private lateinit var notFoundMessage: TextView
    private lateinit var notFoundMessage2: TextView

    //This makes sure that the Navbar is visible in the Customer fragment
    override fun onResume() {
        super.onResume()
        val dashboardActivity = activity as? DashboardActivity
        dashboardActivity?.binding?.apply {
            bottomNavigation.visibility = View.VISIBLE
            plusBtn.visibility = View.VISIBLE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notFoundLayout = view.findViewById(R.id.notFoundLayout)
        notFoundImg = view.findViewById(R.id.notFoundImg)
        notFoundMessage = view.findViewById(R.id.notFoundMessage)
        notFoundMessage2 = view.findViewById(R.id.notFoundMessage2)

        //Initialise necessary elements
        searchView = view.findViewById(R.id.SearchBar)
        gridView = view.findViewById(R.id.CustomerGridView)
        allFilterButton = view.findViewById(R.id.AllFilter)
        prospectFilterButton = view.findViewById(R.id.ProspectFilter)
        leadsFilterButton= view.findViewById(R.id.LeadsFilter)
        referralsFilterButton = view.findViewById(R.id.ReferralsFilter)
        sbHelper = SupabaseHelper()

        //Executes this code block after 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            shimmerFrameLayout = view.findViewById(R.id.shimmerCustomers)
            shimmerFrameLayout.stopShimmer()

            notFoundLayout.visibility = View.GONE
            shimmerFrameLayout.visibility = View.GONE
            gridView.visibility = View.VISIBLE

            lifecycleScope.launch {
                //Gets the customers from the database
                //And updates the customer grid
                customers = sbHelper.getAllCustomers()

                filteredCustomers = customers
                selectButton(allFilterButton)
                updateCustomerGrid(gridView, filteredCustomers)

                setUpSearchView()
            }

            //Filter buttons listeners

            allFilterButton.setOnClickListener {
                filteredCustomers = customers
                updateCustomerGrid(gridView, filteredCustomers)
                selectButton(allFilterButton)
            }
            leadsFilterButton.setOnClickListener {
                filteredCustomers = customers.filter { it.CustomerType == "Leads" }
                updateCustomerGrid(gridView, filteredCustomers)
                selectButton(leadsFilterButton)
            }
            referralsFilterButton.setOnClickListener {
                filteredCustomers = customers.filter { it.CustomerType == "Referrals" }
                updateCustomerGrid(gridView, filteredCustomers)
                selectButton(referralsFilterButton)
            }
            prospectFilterButton.setOnClickListener {
                filteredCustomers = customers.filter { it.CustomerType == "Prospect" }
                updateCustomerGrid(gridView, filteredCustomers)
                selectButton(prospectFilterButton)
            }
        }, 2000)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_customers, container, false)
    }

    //This allows the user to search for a customer by their name
    private fun setUpSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchCustomersByName(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchCustomersByName(it) }
                return true
            }
        })
    }

    //Filters the customer grid by the users search input
    private fun searchCustomersByName(query: String) {
        val queryLower = query.lowercase()

        filteredCustomers = customers.filter { customer ->
            customer.CustomerName.lowercase().contains(queryLower)
        }
        updateCustomerGrid(gridView, filteredCustomers)

        notFoundLayout.visibility = View.GONE
        shimmerFrameLayout.visibility = View.GONE
        gridView.visibility = View.VISIBLE

        if(filteredCustomers.isEmpty()){
            notFoundMessage2.text = getString(R.string.try_entering_a_different_search_term)
            notFoundLayout.visibility = View.VISIBLE
            shimmerFrameLayout.visibility = View.GONE
            gridView.visibility = View.GONE
        }
    }

    //Changes the theme of the selected filter button
    private fun selectButton(selectedButton: Button) {
        allFilterButton.isSelected = selectedButton == allFilterButton
        prospectFilterButton.isSelected = selectedButton == prospectFilterButton
        leadsFilterButton.isSelected = selectedButton == leadsFilterButton
        referralsFilterButton.isSelected = selectedButton == referralsFilterButton
    }

    //Updates the customer grid and sets on click listeners for each grid item
    private fun updateCustomerGrid(gridView: GridView, filteredCustomers: List<CustomerModel>) {
        val adapter = CustomerAdapter(requireContext(), filteredCustomers)
        gridView.adapter = adapter

        gridView.setOnItemClickListener { _, _, position, _ ->
            val selectedCustomer = filteredCustomers[position]

            val fragment = ViewCustomerFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("customer", selectedCustomer)
                }
            }

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}
