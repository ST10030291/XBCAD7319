package com.example.xbcad7319_vucadigital.models

// StackOverflow post
// Titled: Is Kotlin data class serializable by default?
// Posted by: Zohab Ali
// Available at: https://stackoverflow.com/questions/61241155/is-kotlin-data-class-serializable-by-default

import kotlinx.serialization.Serializable

@Serializable
data class CustomerModel(
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
@Serializable
data class NotificationHistoryModel(
    val id: String? = null,
    val customerName: String,
    val message: String,
    val dateTime: String? = null
)
@Serializable
data class AchievementModel(
    val id: String? = null,
    val Name: String,
    val Description: String,
    val ImageUrl: String,
    val Target: Int,
    var Current: Int,
    var Status: String,
) : java.io.Serializable


