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
    private lateinit var customers: List<CustomerModel>

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

        val gridView: GridView = view.findViewById(R.id.CustomerGridView)
        sbHelper = SupabaseHelper()

        Handler(Looper.getMainLooper()).postDelayed({
            val shimmerLayout = view.findViewById<ShimmerFrameLayout>(R.id.shimmerCustomers)
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            gridView.visibility = View.VISIBLE

            lifecycleScope.launch {
                updateCustomerGrid(view)
                gridView.adapter = CustomerAdapter(requireContext(), customers)

                gridView.setOnItemClickListener { _, _, position, _ ->
                    val selectedCustomer = customers[position]
                    openCustomerDetails(selectedCustomer)
                }
            }
        }, 2000)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_customers, container, false)
    }

    private suspend fun updateCustomerGrid(view: View) {
        customers = sbHelper.getAllCustomers()
        Log.d("CustomersFragment", "Customers: $customers")
        val gridView: GridView = view.findViewById(R.id.CustomerGridView)
        gridView.adapter = CustomerAdapter(requireContext(), customers)
    }

    private fun openCustomerDetails(customer: CustomerModel) {
        val fragment = ViewCustomerFragment().apply {
            arguments = Bundle().apply {
                putSerializable("customer", customer)
            }
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
