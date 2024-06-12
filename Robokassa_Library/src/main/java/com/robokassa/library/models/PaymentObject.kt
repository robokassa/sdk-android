package com.robokassa.library.models

import com.google.gson.annotations.SerializedName

enum class PaymentObject {
    @SerializedName("commodity")
    COMMODITY,
    @SerializedName("excise")
    EXCISE,
    @SerializedName("job")
    JOB,
    @SerializedName("service")
    SERVICE,
    @SerializedName("gambling_bet")
    GAMBLING_BET,
    @SerializedName("gambling_prize")
    GAMBLING_PRIZE,
    @SerializedName("lottery")
    LOTTERY,
    @SerializedName("lottery_prize")
    LOTTERY_PRIZE,
    @SerializedName("intellectual_activity")
    INTELLECTUAL_ACTIVITY,
    @SerializedName("payment")
    PAYMENT,
    @SerializedName("agent_commission")
    AGENT_COMMISSION,
    @SerializedName("composite")
    COMPOSITE,
    @SerializedName("resort_fee")
    RESORT_FEE,
    @SerializedName("another")
    ANOTHER,
    @SerializedName("property_right")
    PROPERTY_RIGHT,
    @SerializedName("operating_gain")
    NON_OPERATING_GAIN,
    @SerializedName("insurance_premium")
    INSURANCE_PREMIUM,
    @SerializedName("sales_tax")
    SALES_TAX
}