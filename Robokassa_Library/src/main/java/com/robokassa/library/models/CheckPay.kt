package com.robokassa.library.models

enum class CheckRequestCode(val code: String) {
    CHECKING("-1"),
    SUCCESS("0"),
    SIGNATURE_ERROR("1"),
    SHOP_ERROR("2"),
    INVOICE_ZERO_ERROR("3"),
    INVOICE_DOUBLE_ERROR("4"),
    TIMEOUT_ERROR("999"),
    SERVER_ERROR("1000")
}

enum class CheckPayStateCode(val code: String) {
    NOT_INITED("-1"),
    INITED_NOT_PAYED("5"),
    CANCELLED_NOT_PAYED("10"),
    HOLD_SUCCESS("20"),
    PAYED_NOT_TRANSFERRED("50"),
    PAYMENT_PAYBACK("60"),
    PAYMENT_STOPPED("80"),
    PAYMENT_SUCCESS("100")
}