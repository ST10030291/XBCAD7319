package com.example.xbcad7319_vucadigital.db

import android.net.http.HttpResponseCache.install
import android.util.Log
import com.example.xbcad7319_vucadigital.models.CustomerModel
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class SupabaseHelper {
    private lateinit var supabase: SupabaseClient

    fun initializeSupabase(apiKey: String) {
        supabase = createSupabaseClient(
            supabaseUrl = "https://tptruaqxxbujkjbktvnf.supabase.co",
            supabaseKey = apiKey
        ) {
            install(Postgrest)
        }
    }

    suspend fun getAllCustomers(): List<CustomerModel> {

        return supabase.from("customers").select(){
            order(column = "id", order = Order.ASCENDING)
        }.decodeList<CustomerModel>()
    }

    suspend fun addCustomer(customer : CustomerModel) : Boolean{
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val supabaseApiKey = remoteConfig.getString("SUPABASE_API_KEY")
                initializeSupabase(supabaseApiKey)
            }
        }
        try{
            supabase.from("customers").insert(customer)
            return true
        }
        catch (e: Exception){
            Log.e("INS40", "Insertion failed: ${e.message}", e)
            return false
        }
    }

    suspend fun updateCustomer(customer: CustomerModel) : Boolean{
        try{
            supabase.from("Customers").update(customer) {
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

    suspend fun deleteCustomer(id : Long) : Boolean{
        try{
            supabase.from("Customers").delete {
                filter {
                    CustomerModel::id eq id
                    eq("id", id)
                }
            }
            return true
        }
        catch (e: Exception){
            Log.d("DEL40", "Something went wrong! Insertion failed")
            return false
        }
    }
}