package com.robokassa.library.models

import com.google.gson.annotations.SerializedName

enum class PaymentMethod {
    @SerializedName("full_prepayment")
    FULL_PREPAYMENT,
    @SerializedName("prepayment")
    PREPAYMENT,
    @SerializedName("advance")
    ADVANCE,
    @SerializedName("full_payment")
    FULL_PAYMENT,
    @SerializedName("partial_payment")
    PARTIAL_PAYMENT,
    @SerializedName("credit")
    CREDIT,
    @SerializedName("credit_payment")
    CREDIT_PAYMENT
}