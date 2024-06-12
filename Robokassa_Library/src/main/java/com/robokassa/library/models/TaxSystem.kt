package com.robokassa.library.models

enum class TaxSystem (val api: String) {
    OSN("osn"),
    USN_INCOME("usn_income"),
    USN_INCOME_OUTCOME("usn_income_outcome"),
    ESN("esn"),
    PATENT("patent")
}