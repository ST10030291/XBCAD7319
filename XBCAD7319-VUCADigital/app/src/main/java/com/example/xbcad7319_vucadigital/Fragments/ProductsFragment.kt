package com.example.xbcad7319_vucadigital.Fragments

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
import com.example.xbcad7319_vucadigital.Adapters.ProductAdapter
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.db.SupabaseHelper
import com.example.xbcad7319_vucadigital.models.OpportunityModel
import com.example.xbcad7319_vucadigital.models.ProductModel
import kotlinx.coroutines.launch

class ProductsFragment : Fragment() {
    private lateinit var button: Button
    private lateinit var productAdapter: ProductAdapter
    private var productList = mutableListOf<ProductModel>()
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
        return inflater.inflate(R.layout.fragment_products, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button = view.findViewById(R.id.AllFilter)

        button.setOnClickListener {
            val fragment = CreateProductFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.product_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        productAdapter = ProductAdapter(productList)
        recyclerView.adapter = productAdapter

        sbHelper = SupabaseHelper()
        loadProduct()
    }

    private fun loadProduct() {
        lifecycleScope.launch {
            try {
                val products = sbHelper.getAllProducts()
                val filteredProducts = products.filter { it.Type == "Product" }
                productAdapter.updateProducts(filteredProducts)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Couldn't load products from DB", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun loadService() {
        lifecycleScope.launch {
            try {
                val products = sbHelper.getAllProducts()
                val filteredProducts = products.filter { it.Type == "Service" }
                productAdapter.updateProducts(filteredProducts)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Couldn't load products from DB", Toast.LENGTH_SHORT).show()
            }
        }
    }

}