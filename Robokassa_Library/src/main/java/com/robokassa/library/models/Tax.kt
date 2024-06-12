package com.robokassa.library.models

import com.google.gson.annotations.SerializedName

enum class Tax {
    @SerializedName("none")
    NONE,
    @SerializedName("vat0")
    VAT_0,
    @SerializedName("vat10")
    VAT_10,
    @SerializedName("vat110")
    VAT_110,
    @SerializedName("vat20")
    VAT_20,
    @SerializedName("vat120")
    VAT_120
}