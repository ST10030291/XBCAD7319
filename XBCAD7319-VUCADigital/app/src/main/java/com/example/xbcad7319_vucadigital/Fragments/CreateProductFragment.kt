package com.example.xbcad7319_vucadigital.Fragments

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
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.db.SupabaseHelper
import com.example.xbcad7319_vucadigital.models.CustomerModel
import com.example.xbcad7319_vucadigital.models.OpportunityModel
import com.example.xbcad7319_vucadigital.models.ProductModel
import com.google.android.gms.common.api.Response

class CreateProductFragment : Fragment() {


    private lateinit var productTypeSpinner: Spinner
    private lateinit var productName: EditText
    private lateinit var description: EditText
    private lateinit var price: EditText
    private lateinit var addImage: Button
    private lateinit var createButton: Button
    private lateinit var backButton: ImageView
    private lateinit var image: ImageView
    private var uri: Uri? = null
    private lateinit var galleryImage : ImageView
    private lateinit var cameraImageUrl : Uri
    private lateinit var customer: CustomerModel
    private lateinit var sbHelper: SupabaseHelper
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

        productName = view.findViewById(R.id.productNameInput)
        description = view.findViewById(R.id.descriptionInput)
        productTypeSpinner = view.findViewById(R.id.productTypeInput)
        price = view.findViewById(R.id.priceInput)
        addImage = view.findViewById(R.id.AddImageButton)
        image = view.findViewById(R.id.imageView)
        createButton = view.findViewById(R.id.createProductButton)
        backButton = view.findViewById(R.id.back_btn)

        createButton.setOnClickListener {
            createOpportunity()
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
            pickImage.launch("image/*")
        }

        return view
    }

    private fun createOpportunity() {
        // Retrieve values from the inputs
        val productName = productName.text.toString()
        val description = description.text.toString()
        val priceString = price.text.toString()
        val price: Double = priceString.toDouble()
        val productTypeSpinner = productTypeSpinner.selectedItem.toString()
        val imgUrl = image.toString()
        if (!validateInputs(productName, description, price, productTypeSpinner, imgUrl)) return
        val product = ProductModel(
            ProductName = productName,
            Type = productTypeSpinner,
            Description = description,
            Price = price,
            Image = imgUrl
        )

        /*val firebaseAuth = FirebaseAuth.getInstance().currentUser
        uri?.let{
            val storageReference = FirebaseStorage.getInstance().reference.child("Product Images")
                .putFile(it).addOnSuccessListener { image->
                    image.metadata!!.reference!!.downloadUrl.addOnSuccessListener { url ->
                        val imgUrl = url.toString()

                        val newTask = Tasks(taskID, userID, taskName, description, category, taskDate, startTime, endTime,timeDifference , imgUrl)
                        databaseReference.child(taskID).setValue(newTask)
                    }
                }
        }*/

        Log.d(product.ProductName, "${product.ProductName} saved successfully!")
        Toast.makeText(requireContext(), "Product created successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun validateInputs(
        productName: String,
        description: String,
        price : Double,
        productTypeSpinner: String,
        image: String
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
            image.isEmpty() -> {
                showToast("Please upload an image.")
                false
            }
            else -> true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}