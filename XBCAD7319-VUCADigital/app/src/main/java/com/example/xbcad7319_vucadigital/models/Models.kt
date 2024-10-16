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
    val Products: List<CustomerProduct>
) : java.io.Serializable

@Serializable
class TaskModel(
    val id: String?=null,
    val name: String,
    val category: String,
    val description: String,
    val personAssigned: String,
    val startDate: String,
    val endDate: String,
    val priorityLevel: String
)

@Serializable
class OpportunityModel(
    val id: String? = null,
    val Stage: String,
    val TotalValue: String,
    val LeadStatus: String,
    val CustomerName: String,
    val Priority: String,
    val Status: String,
    val CreationDate: String
)

@Serializable
class ProductModel(
    val id: String? = null,
    val ProductName: String,
    val Type: String,
    val Description: String,
    val Price: String,
    //val Image: String Will have to confirm if the image is necessary
)

@Serializable
class CustomerProduct(
    val id: String? = null,
    val ProductName: String,
    val ContractStart: String,
    val ContractEnd: String,
    val ContractTerm: String,
    val ServiceProvider: String,
    val Status: String,
)


