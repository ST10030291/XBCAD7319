package com.example.xbcad7319_vucadigital.Fragments

import android.content.ContentResolver
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.content.MediaType
import androidx.lifecycle.lifecycleScope
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.db.SupabaseHelper
import com.example.xbcad7319_vucadigital.models.CustomerModel
import com.example.xbcad7319_vucadigital.models.OpportunityModel
import com.example.xbcad7319_vucadigital.models.ProductModel
import com.google.android.gms.common.api.Response
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream

class CreateProductFragment : Fragment() {


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
        addImage = view.findViewById(R.id.AddImageButton)
        image = view.findViewById(R.id.imageView)
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

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()){
            image.setImageURI(it)
            if(it != null){
                uri = it
            }
        }

        addImage.setOnClickListener {
            if(productTypeSpinner.selectedItem.toString() == "Service"){
                Toast.makeText(requireContext(), "Services, do not need an image.", Toast.LENGTH_SHORT).show()
            }else{
                pickImage.launch("image/*")
            }

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
        if (!validateInputsProduct(productName, description, price, productTypeSpinner, imgUrl)) return
        //uploadImageToSupabase(uri)

       /* lifecycleScope.launch {
           // val isInserted = sbHelper.uploadImageToStorage(uri, requireContext())
          //  imgUrl = isInserted
        }*/

        uri?.let{
            val storageReference = FirebaseStorage.getInstance().reference.child("Product Images/${System.currentTimeMillis()}.jpg")
                .putFile(it)
                .addOnSuccessListener { image ->
                    image.metadata!!.reference!!.downloadUrl.addOnSuccessListener { url ->
                        val imgUrl = url.toString()

                        val product = ProductModel(
                            ProductName = productName,
                            Type = productTypeSpinner,
                            Description = description,
                            Price = price,
                            Image = imgUrl
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
                }
        }
    }
    private fun createService() {
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
                            Price = price,
                            Image = imgUrl
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
        productName: String,
        description: String,
        price : Double,
        productTypeSpinner: String,
        imgUrl: String
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
            imgUrl == null.toString() -> {
                showToast("Please upload an image.")
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

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}