package com.example.xbcad7319_vucadigital.db

import android.content.Context
import android.net.Uri
import java.io.InputStream
import android.net.http.HttpResponseCache.install
import android.util.Log
import com.example.xbcad7319_vucadigital.models.CustomerModel
import com.example.xbcad7319_vucadigital.models.OpportunityModel
import com.example.xbcad7319_vucadigital.models.ProductModel
import com.example.xbcad7319_vucadigital.models.TaskModel
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SupabaseHelper {
    private lateinit var supabase: SupabaseClient

    private fun initializeSupabase(apiKey: String) {
        supabase = createSupabaseClient(
            supabaseUrl = "https://tptruaqxxbujkjbktvnf.supabase.co",
            supabaseKey = apiKey
        ) {
            install(Postgrest)
        }
    }

    private suspend fun fetchSupabaseApiKeyAndInitialize(): Boolean {
        return suspendCoroutine { continuation ->
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val supabaseApiKey = remoteConfig.getString("SUPABASE_API_KEY")
                    initializeSupabase(supabaseApiKey)
                    continuation.resume(true)
                } else {
                    continuation.resume(false)
                }
            }
        }
    }

    suspend fun getAllCustomers(): List<CustomerModel> {
        val isInitialized = fetchSupabaseApiKeyAndInitialize()

        if (isInitialized) {
            return supabase.from("customers").select {
                order(column = "id", order = Order.ASCENDING)
            }.decodeList<CustomerModel>()
        } else {
            throw Exception("Supabase initialization failed.")
        }
    }
    suspend fun getAllCustomersNames(): List<String> {
        val isInitialized = fetchSupabaseApiKeyAndInitialize()

        if (isInitialized) {
            return supabase
                .from("customers")
                .select{order(column = "id", order = Order.ASCENDING)}
                .decodeList<CustomerModel>()
                .map { it.CustomerName } 
        } else {
            throw Exception("Supabase initialization failed.")
        }
    }


    suspend fun addCustomer(customer : CustomerModel) : Boolean{
        val isInitialized = fetchSupabaseApiKeyAndInitialize()

        if (isInitialized) {
            supabase.from("customers").insert(customer)
            return true
        } else {
            throw Exception("Supabase initialization failed.")
        }
    }

    suspend fun updateCustomer(customer: CustomerModel) : Boolean{
        try{
            supabase.from("customers").update(customer) {
                filter {
                    CustomerModel::id eq customer.id
                    customer.id?.let { eq("id", it) }
                }
            }
            return true
        }
        catch (e: Exception){
            Log.d("UPD40", "Something went wrong! Update failed")
            return false
        }
    }

    suspend fun deleteCustomer(id : String) : Boolean{
        val isInitialized = fetchSupabaseApiKeyAndInitialize()
        if (isInitialized) {
            try{
                supabase.from("customers").delete {
                    filter {
                        CustomerModel::id eq id
                        eq("id", id)
                    }
                }
                return true
            }
            catch (e: Exception){
                Log.e("INS40", "Deletion failed: ${e.message}", e)
                return false
            }
        }else{
            throw Exception("Supabase initialization failed.")
        }

    }

    suspend fun getCustomerCount(): Int {
        val isInitialized = fetchSupabaseApiKeyAndInitialize()

        if (isInitialized) {
            try {
                // Fetch all customers and get the size of the list for the count
                val result = supabase.from("customers").select().decodeList<CustomerModel>()
                return result.size // Return the count of customers
            } catch (e: Exception) {
                Log.e("DB_ERROR", "Failed to get customer count: ${e.message}", e)
                return 0 // Return 0 in case of an error
            }
        } else {
            throw Exception("Supabase initialization failed.")
        }
    }

    suspend fun getOpportunityCount(): Int {
        val isInitialized = fetchSupabaseApiKeyAndInitialize()

        if (isInitialized) {
            try {
                val result = supabase.from("Opportunity").select().decodeList<OpportunityModel>()
                return result.size
            } catch (e: Exception) {
                Log.e("DB_ERROR", "Failed to get Opportunities Count: ${e.message}", e)
                return 0
            }
        } else {
            throw Exception("Supabase initialization failed.")
        }
    }

    //opportunities
    suspend fun addOpportunities(opportunity : OpportunityModel) : Boolean{
        val isInitialized = fetchSupabaseApiKeyAndInitialize()

        if (isInitialized) {
            supabase.from("Opportunity").insert(opportunity)
            return true
        } else {
            throw Exception("Supabase initialization failed.")
        }
    }

    suspend fun updateOpportunity(opportunity: OpportunityModel) : Boolean{
        try{
            supabase.from("Opportunity").update(opportunity) {
                filter {
                    OpportunityModel::id eq opportunity.id
                    //TaskModel.id?.let { eq("id", it) }
                }
            }
            return true
        }
        catch (e: Exception){
            Log.d("UPD40", "Something went wrong! Update failed")
            return false
        }
    }

    suspend fun deleteOpportunity(id : String) : Boolean{
        val isInitialized = fetchSupabaseApiKeyAndInitialize()
        if (isInitialized) {
            try{
                supabase.from("Opportunity").delete {
                    filter {
                        OpportunityModel::id eq id
                        eq("id", id)
                    }
                }
                return true
            }
            catch (e: Exception){
                Log.e("DEL40", "Deletion failed: ${e.message}", e)
                return false
            }
        }else{
            throw Exception("Supabase initialization failed.")
        }

    }

    suspend fun getAllOpportunities(): List<OpportunityModel> {
        val isInitialized = fetchSupabaseApiKeyAndInitialize()

        if (isInitialized) {
            return supabase.from("Opportunity").select {
                order(column = "id", order = Order.ASCENDING)
            }.decodeList<OpportunityModel>()
        } else {
            throw Exception("Supabase initialization failed.")
        }
    }

    suspend fun fetchTasksByMonth(): ArrayList<Int> {
        val isInitialized = fetchSupabaseApiKeyAndInitialize()
        val tasksByMonthList = ArrayList<Int>()

        if (isInitialized) {
            val result = supabase.from("tasks")
                .select { order(column = "id", order = Order.ASCENDING) }
                .decodeList<TaskModel>()
                .map { it.startDate }

            val tasksGroupedByMonth = IntArray(12)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            result.forEach { task ->
                val taskDate: Date? = dateFormat.parse(task)
                taskDate?.let {
                    val calendar = Calendar.getInstance().apply { time = it }
                    val monthIndex = calendar.get(Calendar.MONTH)
                    tasksGroupedByMonth[monthIndex]++
                }
            }

            tasksByMonthList.addAll(tasksGroupedByMonth.toList())
        } else {
            throw Exception("Supabase initialization failed.")
        }

        return tasksByMonthList
    }


    //products

  /*  suspend fun uploadImageToStorage(imageUri: Uri, context: Context): String {
        val isInitialized = fetchSupabaseApiKeyAndInitialize()
        if (isInitialized) {
            val inputStream = context.contentResolver.openInputStream(imageUri)

            val fileName = "${UUID.randomUUID()}.jpg"
            val upload = supabase.storage.from("product_images").upload(fileName, inputStream)


            val publicUrl = supabase.storage.from("product_images").getPublicUrl(fileName)
            return publicUrl
        } else {
            throw Exception("Supabase initialization failed.")
        }
    }*/




    suspend fun addProducts(product : ProductModel) : Boolean{
        val isInitialized = fetchSupabaseApiKeyAndInitialize()

        if (isInitialized) {
            supabase.from("products").insert(product)
            return true
        } else {
            throw Exception("Supabase initialization failed.")
        }
    }

    suspend fun updateProducts(product : ProductModel) : Boolean{
        try{
            supabase.from("products").update(product) {
                filter {
                    ProductModel::id eq product.id
                    //TaskModel.id?.let { eq("id", it) }
                }
            }
            return true
        }
        catch (e: Exception){
            Log.d("UPD40", "Something went wrong! Update failed")
            return false
        }
    }

    suspend fun deleteProducts(id : String) : Boolean{
        val isInitialized = fetchSupabaseApiKeyAndInitialize()
        if (isInitialized) {
            try{
                supabase.from("products").delete {
                    filter {
                        ProductModel::id eq id
                        eq("id", id)
                    }
                }
                return true
            }
            catch (e: Exception){
                Log.e("DEL40", "Deletion failed: ${e.message}", e)
                return false
            }
        }else{
            throw Exception("Supabase initialization failed.")
        }

    }

    suspend fun getAllProducts(): List<ProductModel> {
        val isInitialized = fetchSupabaseApiKeyAndInitialize()

        if (isInitialized) {
            return supabase.from("products").select {
                order(column = "id", order = Order.ASCENDING)
            }.decodeList<ProductModel>()
        } else {
            throw Exception("Supabase initialization failed.")
        }
    }

    suspend fun addTask(task : TaskModel) : Boolean{
        val isInitialized = fetchSupabaseApiKeyAndInitialize()

        if (isInitialized) {
            supabase.from("tasks").insert(task)
            return true
        } else {
            throw Exception("Supabase initialization failed.")
        }
    }

    suspend fun updateTask(task: TaskModel) : Boolean{
        try{
            supabase.from("tasks").update(task) {
                filter {
                    TaskModel::id eq task.id
                    //TaskModel.id?.let { eq("id", it) }
                }
            }
            return true
        }
        catch (e: Exception){
            Log.d("UPD40", "Something went wrong! Update failed")
            return false
        }
    }

    suspend fun deleteTask(id : String) : Boolean{
        val isInitialized = fetchSupabaseApiKeyAndInitialize()
        if (isInitialized) {
            try{
                supabase.from("tasks").delete {
                    filter {
                        TaskModel::id eq id
                        eq("id", id)
                    }
                }
                return true
            }
            catch (e: Exception){
                Log.e("DEL40", "Deletion failed: ${e.message}", e)
                return false
            }
        }else{
            throw Exception("Supabase initialization failed.")
        }

    }

    suspend fun getAllTasks(): List<TaskModel> {
        val isInitialized = fetchSupabaseApiKeyAndInitialize()

        if (isInitialized) {
            return supabase.from("tasks").select {
                order(column = "id", order = Order.ASCENDING)
            }.decodeList<TaskModel>()
        } else {
            throw Exception("Supabase initialization failed.")
        }
    }

    suspend fun getCustomerNameUsingID(id : String) : CustomerModel{
        val isInitialized = fetchSupabaseApiKeyAndInitialize()

        if (isInitialized) {
            return supabase.from("customers")
                .select {
                    filter {
                        CustomerModel::id eq id
                        eq("id", id)
                    }
                }
                .decodeSingle<CustomerModel>()
        } else {
            throw Exception("Supabase initialization failed.")
        }
    }
}

