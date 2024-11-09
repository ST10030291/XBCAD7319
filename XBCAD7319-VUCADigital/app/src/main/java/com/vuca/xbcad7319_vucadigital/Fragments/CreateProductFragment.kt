package com.vuca.xbcad7319_vucadigital.Fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.vuca.xbcad7319_vucadigital.R
import com.vuca.xbcad7319_vucadigital.db.SupabaseHelper
import com.vuca.xbcad7319_vucadigital.models.CustomerModel
import com.vuca.xbcad7319_vucadigital.models.ProductModel
import kotlinx.coroutines.launch

class CreateProductFragment : Fragment() {

    //declaring variables globally
    private lateinit var productTypeSpinner: Spinner
    private lateinit var productName: EditText
    private lateinit var description: EditText
    private lateinit var price: EditText
    private lateinit var addImage: Button
    private lateinit var createButton: Button
    private lateinit var backButton: ImageView
    private lateinit var image: ImageView
    private var imageUrl : String? = null
    private var uri: Uri? = null
    private lateinit var galleryImage : ImageView
    private lateinit var cameraImageUrl : Uri
    private lateinit var customer: CustomerModel
    private lateinit var sbHelper: SupabaseHelper
    private lateinit var imgUrl: String
    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()){
        galleryImage.setImageURI(null)
        galleryImage.setImageURI(cameraImageUrl)
        uri = cameraImageUrl
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_product, container, false)
        sbHelper = SupabaseHelper()
        productName = view.findViewById(R.id.productNameInput)
        description = view.findViewById(R.id.descriptionInput)
        productTypeSpinner = view.findViewById(R.id.productTypeInput)
        price = view.findViewById(R.id.priceInput)

        createButton = view.findViewById(R.id.createProductButton)
        backButton = view.findViewById(R.id.back_btn)

        createButton.setOnClickListener {
            if(productTypeSpinner.selectedItem.toString() == "Service"){
                createService()
            }else if(productTypeSpinner.selectedItem.toString() == "Product"){
                createProduct()
            }

        }
        backButton.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }
        return view
    }


    private fun createProduct() {
        // Retrieve values from the inputs
        val productName = productName.text.toString()
        val description = description.text.toString()
        val priceString = price.text.toString()
        val price: Double = priceString.toDouble()
        var imgUrl: String = uri.toString()
        val productTypeSpinner = productTypeSpinner.selectedItem.toString()
        if (!validateInputsProduct(productName, description, price, productTypeSpinner)) return
        val product = ProductModel(
            ProductName = productName,
            Type = productTypeSpinner,
            Description = description,
            Price = price
        )

        lifecycleScope.launch {
            //saves the product to the database
            val isInserted = sbHelper.addProducts(product)

            if (isInserted) {
                Log.d(product.ProductName, "${product.ProductName} saved successfully!")
                Toast.makeText(requireContext(), "${product.Type} created successfully!", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                Log.d(product.ProductName, "${product.ProductName} failed!")
                Toast.makeText(requireContext(), "${product.Type} creation failed!", Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun createService() {
        //saves to the database
        // Retrieve values from the inputs
        val productName = productName.text.toString()
        val description = description.text.toString()
        val priceString = price.text.toString()
        val price: Double = priceString.toDouble()
        //var imgUrl: String = null.toString()
        val productTypeSpinner = productTypeSpinner.selectedItem.toString()
        if (!validateInputsService(productName, description, price, productTypeSpinner)) return


        val imgUrl = null.toString()

        val product = ProductModel(
            ProductName = productName,
            Type = productTypeSpinner,
            Description = description,
            Price = price
        )

        lifecycleScope.launch {
            val isInserted = sbHelper.addProducts(product)

            if (isInserted) {
                Log.d(product.ProductName, "${product.ProductName} saved successfully!")
                Toast.makeText(requireContext(), "${product.Type} created successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(product.ProductName, "${product.ProductName} failed!")
                Toast.makeText(requireContext(), "${product.Type} creation failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputsProduct(

        //validates user inputs
        productName: String,
        description: String,
        price : Double,
        productTypeSpinner: String
    ): Boolean {
        return when {
            productName.isEmpty() -> {
                showToast("Empty Product Name! Please enter a product name.")
                false
            }
            price == null -> {
                showToast("Please enter a value.")
                false
            }
            description.isEmpty()  -> {
                showToast("Empty Description! Please enter a description.")
                false
            }
            productTypeSpinner == "" -> {
                showToast("Product Type assigned not selected! Please select a product type.")
                false
            }
            else -> true
        }
    }
    private fun validateInputsService(
        productName: String,
        description: String,
        price : Double,
        productTypeSpinner: String
    ): Boolean {
        return when {
            productName.isEmpty() -> {
                showToast("Empty Service Name! Please enter a service name.")
                false
            }
            price == null -> {
                showToast("Please enter a value.")
                false
            }
            description.isEmpty()  -> {
                showToast("Empty Description! Please enter a description.")
                false
            }
            productTypeSpinner == "" -> {
                showToast("Product Type assigned not selected! Please select a service type.")
                false
            }
            else -> true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}