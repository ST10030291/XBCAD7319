package com.vuca.xbcad7319_vucadigital.Fragments

import android.app.AlertDialog
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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import com.vuca.xbcad7319_vucadigital.Activites.DashboardActivity
import com.vuca.xbcad7319_vucadigital.Adapters.ProductAdapter
import com.vuca.xbcad7319_vucadigital.Adapters.ServiceAdapter
import com.vuca.xbcad7319_vucadigital.R
import com.vuca.xbcad7319_vucadigital.db.SupabaseHelper
import com.vuca.xbcad7319_vucadigital.models.ProductModel
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductsFragment : Fragment() {
    //declaring all variables globally
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

    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var notFoundLayout: LinearLayout
    private lateinit var notFoundImg: ImageView
    private lateinit var notFoundMessage: TextView
    private lateinit var notFoundMessage2: TextView

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

        notFoundLayout = view.findViewById(R.id.notFoundLayout)
        notFoundImg = view.findViewById(R.id.notFoundImg)
        notFoundMessage = view.findViewById(R.id.notFoundMessage)
        notFoundMessage2 = view.findViewById(R.id.notFoundMessage2)

        allButton = view.findViewById(R.id.AllFilter)
        productButton = view.findViewById(R.id.ProductsFilter)
        serviceButton = view.findViewById(R.id.ServicesFilter)

        searchView = view.findViewById(R.id.productAndServicesSearch)
        recyclerView = view.findViewById(R.id.product_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)//allows to list items to show next to each other
        productAdapter = ProductAdapter(productList, ::onEditProduct, ::onDeleteProduct)//calling the Product Adapter
        recyclerView.adapter = productAdapter


        recyclerViewService = view.findViewById(R.id.service_recycler_view)
        recyclerViewService.layoutManager = GridLayoutManager(requireContext(), 2)//allows to list items to show next to each other
        serviceAdapter = ServiceAdapter(serviceList, ::onEditProduct, ::onDeleteService)//calling the Service Adapter
        recyclerViewService.adapter = serviceAdapter

        sbHelper = SupabaseHelper()//calling the supabasehelper

        Handler(Looper.getMainLooper()).postDelayed({
            shimmerFrameLayout = view.findViewById(R.id.shimmerCustomers)
            shimmerFrameLayout.stopShimmer()

            notFoundLayout.visibility = View.GONE
            shimmerFrameLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerViewService.visibility = View.VISIBLE

            loadProduct()
            loadService()
            setUpSearchView()
            allButton.isSelected = true
            allButton.setOnClickListener {
                //displays all products and services
                allButton.setBackgroundResource(R.drawable.filter_btn_selected)
                productButton.setBackgroundResource(R.drawable.filter_btn_border)
                serviceButton.setBackgroundResource(R.drawable.filter_btn_border)
                recyclerView.visibility = RecyclerView.VISIBLE
                recyclerViewService.visibility = RecyclerView.VISIBLE
            }

            productButton.setOnClickListener {
                //displays only products
                productButton.setBackgroundResource(R.drawable.filter_btn_selected)
                allButton.setBackgroundResource(R.drawable.filter_btn_border)
                serviceButton.setBackgroundResource(R.drawable.filter_btn_border)
                recyclerViewService.visibility = RecyclerView.GONE
                recyclerView.visibility = RecyclerView.VISIBLE

                if(productList.isEmpty()){
                    notFoundMessage2.text = getString(R.string.try_choosing_another_filter)
                    notFoundLayout.visibility = View.VISIBLE
                    shimmerFrameLayout.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                }
                //loadProduct()
            }

            serviceButton.setOnClickListener {
                //displays only services
                serviceButton.setBackgroundResource(R.drawable.filter_btn_selected)
                allButton.setBackgroundResource(R.drawable.filter_btn_border)
                productButton.setBackgroundResource(R.drawable.filter_btn_border)
                recyclerView.visibility = RecyclerView.GONE
                recyclerViewService.visibility = RecyclerView.VISIBLE

                if(serviceList.isEmpty()){
                    notFoundMessage2.text = getString(R.string.try_choosing_another_filter)
                    notFoundLayout.visibility = View.VISIBLE
                    shimmerFrameLayout.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                }
                //loadService()
            }
        },2000)


    }
    private fun checkLists() {
        //method to ensure none of the recyclerviews are shown when certain products or services are filtered
        if (productList.isNotEmpty()) {

            notFoundLayout.visibility = View.GONE
            recyclerView.visibility = RecyclerView.VISIBLE
            recyclerViewService.visibility = RecyclerView.GONE


        } else if (serviceList.isNotEmpty()) {

            notFoundLayout.visibility = View.GONE
            recyclerViewService.visibility = RecyclerView.VISIBLE
            recyclerView.visibility = RecyclerView.GONE
        }
        else {
            notFoundLayout.visibility = View.GONE
            recyclerView.visibility = RecyclerView.VISIBLE
            recyclerViewService.visibility = RecyclerView.VISIBLE
        }
    }

    private fun loadProduct() {
        //method that gets the products from the database
        lifecycleScope.launch {
            try {
                val products = sbHelper.getAllProducts()
                productss = sbHelper.getAllProducts()
                val filteredProducts = products.filter { it.Type == "Product" }//filtering by only product
                productAdapter.updateProducts(filteredProducts)
                productAdapter.notifyDataSetChanged()
                checkLists()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Couldn't load products from DB", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun loadService() {
        //fetches all the services from the database
        lifecycleScope.launch {
            try {
                val products = sbHelper.getAllProducts()
                services = sbHelper.getAllProducts()
                val filteredProducts = products.filter { it.Type == "Service" }//filtering by services
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
            //method for the searchview that takes in user input
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
        //have to search for this product and display accordingly
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
            notFoundMessage2.text = getString(R.string.try_entering_a_different_search_term)
            notFoundLayout.visibility = View.VISIBLE
            shimmerFrameLayout.visibility = View.GONE
            recyclerView.visibility = View.GONE
        }
    }

    private fun onDeleteProduct(product: ProductModel) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_task, null)
        val deleteDialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        val taskNameTextView: TextView = dialogView.findViewById(R.id.dialog_task_name)
        val messageTextView: TextView = dialogView.findViewById(R.id.dialog_message)
        val cancelButton: Button = dialogView.findViewById(R.id.button_cancel)
        val deleteButton: Button = dialogView.findViewById(R.id.button_delete)

        //just setting the dialog delete to show this
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
                    //product is deleted from database
                    sbHelper.deleteProducts(product.id!!)
                    withContext(Dispatchers.Main) {
                        productAdapter.removeProduct(product)//and removed from the adapter
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
                    // Delete the service from the database
                    sbHelper.deleteProducts(product.id!!)
                    withContext(Dispatchers.Main) {
                        serviceAdapter.removeService(product)//removes it from the service adpater
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

        // Populate fields with the product's current values
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
            //takes the user input and saves it to the existing product to update any changes which were made
            val updatedProduct = product.copy(
                ProductName = productName.text.toString(),
                        Type = productTypeSpinner.selectedItem.toString(),
                Description = description.text.toString(),
            Price = price
            )

            try {
                // Update the products in the database
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