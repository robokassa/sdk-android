package com.robokassa.library.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Receipt(
    @SerializedName("sno")
    val sno: TaxSystem? = null,
    @SerializedName("items")
    val items: List<ReceiptItem>
) : Serializable
