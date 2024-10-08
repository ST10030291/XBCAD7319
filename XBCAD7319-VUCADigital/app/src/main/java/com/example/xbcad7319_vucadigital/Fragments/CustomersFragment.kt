package com.example.xbcad7319_vucadigital.Fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import androidx.lifecycle.lifecycleScope
import com.example.xbcad7319_vucadigital.Activites.DashboardActivity
import com.example.xbcad7319_vucadigital.Adapters.CustomerAdapter
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.db.SupabaseHelper
import com.example.xbcad7319_vucadigital.models.CustomerModel
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class CustomersFragment : Fragment() {
    private lateinit var sbHelper: SupabaseHelper
    private lateinit var customers : List<CustomerModel>
    override fun onResume() {
        super.onResume()

        val dashboardActivity = activity as DashboardActivity
        dashboardActivity.binding.bottomNavigation.visibility = View.VISIBLE
        dashboardActivity.binding.plusBtn.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gridView: GridView = view.findViewById(R.id.CustomerGridView)

        sbHelper = SupabaseHelper()

        Handler(Looper.getMainLooper()).postDelayed({
            view.findViewById<ShimmerFrameLayout>(R.id.shimmerCustomers).stopShimmer()
            view.findViewById<ShimmerFrameLayout>(R.id.shimmerCustomers).visibility = View.GONE
            gridView.visibility = View.VISIBLE
            lifecycleScope.launch {
                updateCustomerGrid(view)
                val customerAdapter = CustomerAdapter(requireContext(), customers)
                gridView.adapter = customerAdapter

                gridView.setOnItemClickListener { _, _, position, _ ->
                    val selectedCustomer = customers[position]

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
        },4000)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_customers, container, false)
        return view
    }

    private suspend fun updateCustomerGrid(view: View) {
        customers = sbHelper.getAllCustomers()
        Log.d("", customers.toString())
        val adapter = CustomerAdapter(requireContext(), customers)
        view.findViewById<GridView>(R.id.CustomerGridView).adapter = adapter
    }

}