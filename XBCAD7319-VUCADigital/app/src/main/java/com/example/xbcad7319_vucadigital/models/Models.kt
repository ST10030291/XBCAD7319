package com.example.xbcad7319_vucadigital.models

import kotlinx.serialization.Serializable

@Serializable
class CustomerModel(
    val id: String? = null,
    val CustomerName: String,
    val TelephoneNumber: String,
    val CustomerEmail: String,
    val AccountNumber: String,
    val BillingAccountNumber: String,
    val CustomerType: String,
    val Products: List<CustomerProductModel>
) : java.io.Serializable

@Serializable
data class TaskModel(
    val id: String?=null,
    var name: String,
    var category: String,
    var description: String,
    var personAssigned: String,
    val startDate: String,
    val endDate: String,
    var priorityLevel: String,
    var status : String,
    val customerID : String?=null
)

@Serializable
data class OpportunityModel(
    val id: String? = null,
    val OpportunityName: String,
    val TotalValue: Double,
    val Stage: String,
    val CustomerName: String,
    val Priority: String,
    val Status: String,
    val CreationDate: String
)

@Serializable
data class ProductModel(
    val id: String? = null,
    val ProductName: String,
    val Type: String,
    val Description: String,
    val Price: Double
)

@Serializable
class CustomerProductModel(
    val id: String? = null,
    val CustomerName: String,
    val ProductName: String,
    val ContractStart: String,
    val ContractEnd: String,
    val ContractTerm: String,
    val ServiceProvider: String,
    val Status: String,
)


