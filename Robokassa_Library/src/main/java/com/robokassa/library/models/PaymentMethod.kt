package com.robokassa.library.models

enum class PaymentMethod(val api: String) {
    FULL_PREPAYMENT("full_prepayment"),
    PREPAYMENT("prepayment"),
    ADVANCE("advance"),
    FULL_PAYMENT("full_payment"),
    PARTIAL_PAYMENT("partial_payment"),
    CREDIT("credit"),
    CREDIT_PAYMENT("credit_payment")
}