package com.example.xbcad7319_vucadigital.db


import android.util.Log
import com.example.xbcad7319_vucadigital.models.CustomerModel
import com.example.xbcad7319_vucadigital.models.TaskModel
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
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
//    Still implementing
//    suspend fun deleteTask(id : String) : Boolean{
//        try{
//            supabase.from("tasks").delete {
//                filter {
//                    eq("id", id)
//                }
//            }
//            return true
//        }
//        catch (e: Exception){
//            Log.d("DEL40", "Something went wrong! Delete process failed.")
//            return false
//        }
//    }

    suspend fun getAllTasks(): List<TaskModel> {
        val isInitialized = fetchSupabaseApiKeyAndInitialize()

        if (isInitialized) {
            return supabase.from("tasks").select{
                order(column = "id", order = Order.ASCENDING)
            }.decodeList<TaskModel>()
        } else {
            throw Exception("Supabase initialization failed.")
        }
    }

}