package com.robokassa.library.models

enum class PaymentObject(val api: String) {
    COMMODITY("commodity"),
    EXCISE("excise"),
    JOB("job"),
    SERVICE("service"),
    GAMBLING_BET("gambling_bet"),
    GAMBLING_PRIZE("gambling_prize"),
    LOTTERY("lottery"),
    LOTTERY_PRIZE("lottery_prize"),
    INTELLECTUAL_ACTIVITY("intellectual_activity"),
    PAYMENT("payment"),
    AGENT_COMMISSION("agent_commission"),
    COMPOSITE("composite"),
    RESORT_FEE("resort_fee"),
    ANOTHER("another"),
    PROPERTY_RIGHT("property_right"),
    NON_OPERATING_GAIN("non-operating_gain"),
    INSURANCE_PREMIUM("insurance_premium"),
    SALES_TAX("sales_tax")
}