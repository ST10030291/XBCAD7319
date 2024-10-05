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
        return supabase.from("Customers").select(){
            order(column = "id", order = Order.ASCENDING)
        }.decodeList<CustomerModel>()
    }

    suspend fun addCustomer(customer : CustomerModel) : Boolean{
        try{
            supabase.from("Customers").insert(customer)
            return true
        }
        catch (e: Exception){
            Log.d("INS40", "Something went wrong! Insertion failed")
            return false
        }
    }

    suspend fun updateCustomer(customer: CustomerModel) : Boolean{
        try{
            supabase.from("Customers").update(customer) {
                filter {
                    CustomerModel::id eq customer.id
                    eq("id", customer.id)
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