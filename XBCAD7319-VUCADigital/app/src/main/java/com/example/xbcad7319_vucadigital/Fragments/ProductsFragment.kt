package com.example.xbcad7319_vucadigital.Fragments

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract.Colors
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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.xbcad7319_vucadigital.Activites.DashboardActivity
import com.example.xbcad7319_vucadigital.Adapters.CustomSpinnerAdapter
import com.example.xbcad7319_vucadigital.Adapters.OpportunityAdapter
import com.example.xbcad7319_vucadigital.Adapters.ProductAdapter
import com.example.xbcad7319_vucadigital.Adapters.ServiceAdapter
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.db.SupabaseHelper
import com.example.xbcad7319_vucadigital.models.OpportunityModel
import com.example.xbcad7319_vucadigital.models.ProductModel
import com.example.xbcad7319_vucadigital.models.TaskModel
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductsFragment : Fragment() {
    private lateinit var allButton: Button
    private lateinit var productAdapter: ProductAdapter
    private var productList = mutableListOf<ProductModel>()
    private lateinit var recyclerView: RecyclerView


    private lateinit var serviceAdapter: ServiceAdapter
    private var serviceList = mutableListOf<ProductModel>()
    private lateinit var recyclerViewService : RecyclerView
    private lateinit var filteredProducts: List<ProductModel>
    private lateinit var productss: List<ProductModel>

    private lateinit var filteredServices: List<ProductModel>
    private lateinit var services: List<ProductModel>

    private lateinit var productButton: Button
    private lateinit var serviceButton: Button
    private lateinit var sbHelper: SupabaseHelper

    private lateinit var searchView: SearchView

    override fun onResume() {
        super.onResume()
        val dashboardActivity = activity as? DashboardActivity
        dashboardActivity?.binding?.apply {
            bottomNavigation.visibility = View.VISIBLE
            plusBtn.visibility = View.VISIBLE
            plusBtn.setOnClickListener {
                dashboardActivity.loadFragment(CreateProductFragment(), true)
                bottomNavigation.visibility = View.GONE
            }
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

        searchView = view.findViewById(R.id.productAndServicesSearch)
        recyclerView = view.findViewById(R.id.product_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        productAdapter = ProductAdapter(productList, ::onEditProduct, ::onDeleteProduct)
        recyclerView.adapter = productAdapter


        recyclerViewService = view.findViewById(R.id.service_recycler_view)
        recyclerViewService.layoutManager = GridLayoutManager(requireContext(), 2)
        serviceAdapter = ServiceAdapter(serviceList, ::onEditProduct, ::onDeleteService)
        recyclerViewService.adapter = serviceAdapter

        sbHelper = SupabaseHelper()

        Handler(Looper.getMainLooper()).postDelayed({
            val shimmerLayout = view.findViewById<ShimmerFrameLayout>(R.id.shimmerCustomers)
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerViewService.visibility = View.VISIBLE

            loadProduct()
            loadService()
            setUpSearchView()

            allButton.setOnClickListener {
                allButton.setBackgroundResource(R.drawable.filter_btn_selected);
                productButton.setBackgroundResource(R.drawable.filter_btn_border);
                serviceButton.setBackgroundResource(R.drawable.filter_btn_border);
                recyclerView.visibility = RecyclerView.VISIBLE
                recyclerViewService.visibility = RecyclerView.VISIBLE
            }

            productButton.setOnClickListener {
                productButton.setBackgroundResource(R.drawable.filter_btn_selected);
                allButton.setBackgroundResource(R.drawable.filter_btn_border);
                serviceButton.setBackgroundResource(R.drawable.filter_btn_border);
                recyclerViewService.visibility = RecyclerView.GONE
                recyclerView.visibility = RecyclerView.VISIBLE
                //loadProduct()
            }

            serviceButton.setOnClickListener {
                serviceButton.setBackgroundResource(R.drawable.filter_btn_selected);
                allButton.setBackgroundResource(R.drawable.filter_btn_border);
                productButton.setBackgroundResource(R.drawable.filter_btn_border);
                recyclerView.visibility = RecyclerView.GONE
                recyclerViewService.visibility = RecyclerView.VISIBLE
                //loadService()
            }
        },2000)


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
                productss = sbHelper.getAllProducts()
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
                services = sbHelper.getAllProducts()
                val filteredProducts = products.filter { it.Type == "Service" }
                serviceAdapter.updateProducts(filteredProducts)
                serviceAdapter.notifyDataSetChanged()
                checkLists()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Couldn't load services from DB", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setUpSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                //recyclerViewService.visibility = RecyclerView.GONE
                query?.let { searchProductsByName(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //recyclerViewService.visibility = RecyclerView.GONE
                newText?.let { searchProductsByName(it) }
                return true
            }
        })
    }

    private fun searchProductsByName(query: String) {
        val queryLower = query.lowercase()

        filteredProducts = productss.filter { product ->
            product.ProductName.lowercase().contains(queryLower) &&
                    product.Type == "Product"
        }


        productAdapter.updateProductss(filteredProducts)

        filteredServices = services.filter { service ->
            service.ProductName.lowercase().contains(queryLower) &&
                    service.Type == "Service"
        }

        serviceAdapter.updateServices(filteredServices)
        //recyclerViewService.visibility = RecyclerView.GONE

        // Show a toast message if filteredTasks is empty
        if (filteredProducts.isEmpty()) {
            Toast.makeText(context, "Product/Service \"$query\" not found!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onDeleteProduct(product: ProductModel) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_task, null)
        val deleteDialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        val taskNameTextView: TextView = dialogView.findViewById(R.id.dialog_task_name)
        val messageTextView: TextView = dialogView.findViewById(R.id.dialog_message)
        val cancelButton: Button = dialogView.findViewById(R.id.button_cancel)
        val deleteButton: Button = dialogView.findViewById(R.id.button_delete)

        messageTextView.text = "Are you sure you want to delete this product?"

        // Set the task name in the dialog
        taskNameTextView.text = product.ProductName

        cancelButton.setOnClickListener {
            deleteDialog.dismiss()
            Toast.makeText(requireContext(), "Product cancelled! No product deleted.", Toast.LENGTH_SHORT).show()
        }

        deleteButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Delete the task from the database
                    sbHelper.deleteProducts(product.id!!)
                    withContext(Dispatchers.Main) {
                        productAdapter.removeProduct(product)
                        Toast.makeText(requireContext(), "Product Success! Product deleted.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("TaskDeleteError", "Error deleting product: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Product failure! Couldn't delete product.", Toast.LENGTH_SHORT).show()
                    }
                }
                deleteDialog.dismiss()
            }
        }

        deleteDialog.show()
    }
    private fun onDeleteService(product: ProductModel) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_task, null)
        val deleteDialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        val taskNameTextView: TextView = dialogView.findViewById(R.id.dialog_task_name)
        val messageTextView: TextView = dialogView.findViewById(R.id.dialog_message)
        val cancelButton: Button = dialogView.findViewById(R.id.button_cancel)
        val deleteButton: Button = dialogView.findViewById(R.id.button_delete)

        messageTextView.text = "Are you sure you want to delete this service?"

        // Set the task name in the dialog
        taskNameTextView.text = product.ProductName

        cancelButton.setOnClickListener {
            deleteDialog.dismiss()
            Toast.makeText(requireContext(), "Service cancelled! No service deleted.", Toast.LENGTH_SHORT).show()
        }

        deleteButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Delete the task from the database
                    sbHelper.deleteProducts(product.id!!)
                    withContext(Dispatchers.Main) {
                        serviceAdapter.removeService(product)
                        Toast.makeText(requireContext(), "Service Success! Service deleted.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("TaskDeleteError", "Error deleting product: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Service failure! Couldn't delete service.", Toast.LENGTH_SHORT).show()
                    }
                }
                deleteDialog.dismiss()
            }
        }

        deleteDialog.show()
    }
    private fun onEditProduct(product: ProductModel) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_product, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Edit Product")

        setupDialogViews(dialogView, product)

        val dialog = builder.create()

        setupDialogButtons(dialog, dialogView, product)

        dialog.show()
    }
    private fun setupDialogButtons(dialog: AlertDialog, dialogView: View, product: ProductModel) {
        dialogView.findViewById<Button>(R.id.cancelEditProduct).setOnClickListener {
            dialog.dismiss() // Close the dialog
        }

        dialogView.findViewById<Button>(R.id.saveEditProduct).setOnClickListener {
            saveProductChanges(dialog, product, dialogView)
        }
    }

    private fun setupDialogViews(dialogView: View, product: ProductModel) {
        // Get references to the dialog views

        val productName : EditText = dialogView.findViewById(R.id.productNameInput)
        val description : EditText = dialogView.findViewById(R.id.descriptionInput)
        val productTypeSpinner : Spinner = dialogView.findViewById(R.id.productTypeInput)
        val price : EditText = dialogView.findViewById(R.id.priceInput)



        val typeOptions = resources.getStringArray(R.array.typeOptions)

        // Populate fields with the task's current values
        productName.setText(product.ProductName)
        productTypeSpinner.setSelection(typeOptions.indexOf(product.Type))
        price.setText(product.Price.toString())
        description.setText(product.Description)
    }
    private fun saveProductChanges(dialog: AlertDialog, product: ProductModel, dialogView: View) {
        val productName : EditText = dialogView.findViewById(R.id.productNameInput)
        val description : EditText = dialogView.findViewById(R.id.descriptionInput)
        val productTypeSpinner : Spinner = dialogView.findViewById(R.id.productTypeInput)
        val priceInput : EditText = dialogView.findViewById(R.id.priceInput)

        val priceString = priceInput.text.toString()
        val price: Double = priceString.toDouble()

        lifecycleScope.launch {
            val updatedProduct = product.copy(
                ProductName = productName.text.toString(),
                        Type = productTypeSpinner.selectedItem.toString(),
                Description = description.text.toString(),
            Price = price
            )

            try {
                // Update the task in the database
                sbHelper.updateProducts(updatedProduct)

                // Update the task in the adapter
                productAdapter.updateProduct(updatedProduct)
                if(updatedProduct.Type == "Service"){
                    serviceAdapter.updateProduct(updatedProduct)
                }

                Toast.makeText(requireContext(), "Operation Success! Updated product.", Toast.LENGTH_SHORT).show()
                dialog.dismiss() // Dismiss only after success
            } catch (e: Exception) {
                Log.e("EditTask", "Error updating product", e) // Log error details
                Toast.makeText(requireContext(), "Operation failure! Couldn't update product.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}