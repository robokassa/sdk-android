package com.robokassa.library.models

enum class Tax(val api: String) {
    NONE("none"),
    VAT_0("vat0"),
    VAT_10("vat10"),
    VAT_110("vat110"),
    VAT_20("vat20"),
    VAT_120("vat120")
}