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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.xbcad7319_vucadigital.Activites.DashboardActivity
import com.example.xbcad7319_vucadigital.Adapters.OpportunityAdapter
import com.example.xbcad7319_vucadigital.Adapters.ProductAdapter
import com.example.xbcad7319_vucadigital.Adapters.ServiceAdapter
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.db.SupabaseHelper
import com.example.xbcad7319_vucadigital.models.OpportunityModel
import com.example.xbcad7319_vucadigital.models.ProductModel
import kotlinx.coroutines.launch

class ProductsFragment : Fragment() {
    private lateinit var allButton: Button
    private lateinit var productAdapter: ProductAdapter
    private var productList = mutableListOf<ProductModel>()
    private lateinit var recyclerView: RecyclerView


    private lateinit var serviceAdapter: ServiceAdapter
    private var serviceList = mutableListOf<ProductModel>()
    private lateinit var recyclerViewService : RecyclerView

    private lateinit var productButton: Button
    private lateinit var serviceButton: Button
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
        allButton = view.findViewById(R.id.AllFilter)
        productButton = view.findViewById(R.id.ProductsFilter)
        serviceButton = view.findViewById(R.id.ServicesFilter)


        recyclerView = view.findViewById(R.id.product_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        productAdapter = ProductAdapter(productList)
        recyclerView.adapter = productAdapter


        recyclerViewService = view.findViewById(R.id.service_recycler_view)
        recyclerViewService.layoutManager = GridLayoutManager(requireContext(), 2)
        serviceAdapter = ServiceAdapter(serviceList)
        recyclerViewService.adapter = serviceAdapter

        sbHelper = SupabaseHelper()


        loadProduct()
        loadService()


        allButton.setOnClickListener {
            recyclerView.visibility = RecyclerView.VISIBLE
            recyclerViewService.visibility = RecyclerView.VISIBLE
        }

        productButton.setOnClickListener {
            recyclerViewService.visibility = RecyclerView.GONE
            recyclerView.visibility = RecyclerView.VISIBLE
            //loadProduct()
        }

        serviceButton.setOnClickListener {
            recyclerView.visibility = RecyclerView.GONE
            recyclerViewService.visibility = RecyclerView.VISIBLE
            //loadService()
        }
    }
    private fun checkLists() {
        if (productList.isNotEmpty()) {


            recyclerView.visibility = RecyclerView.VISIBLE
            recyclerViewService.visibility = RecyclerView.GONE


        } else if (serviceList.isNotEmpty()) {

            recyclerViewService.visibility = RecyclerView.VISIBLE
            recyclerView.visibility = RecyclerView.GONE
        } else {
            recyclerView.visibility = RecyclerView.VISIBLE
            recyclerViewService.visibility = RecyclerView.VISIBLE
        }
    }

    private fun loadProduct() {
        lifecycleScope.launch {
            try {
                val products = sbHelper.getAllProducts()
                val filteredProducts = products.filter { it.Type == "Product" }
                productAdapter.updateProducts(filteredProducts)
                productAdapter.notifyDataSetChanged()
                checkLists()
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
                serviceAdapter.updateProducts(filteredProducts)
                serviceAdapter.notifyDataSetChanged()
                checkLists()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Couldn't load services from DB", Toast.LENGTH_SHORT).show()
            }
        }
    }

}