package com.robokassa.library.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReceiptItem(
    @SerializedName("name")
    val name: String,
    @SerializedName("sum")
    val sum: Double,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("cost")
    val cost: Double? = null,
    @SerializedName("nomenclature_code")
    val nomenclatureCode: String? = null,
    @SerializedName("payment_method")
    val paymentMethod: PaymentMethod? = null,
    @SerializedName("payment_object")
    val paymentObject: PaymentObject? = null,
    @SerializedName("tax")
    val tax: Tax? = null
) : Serializable
