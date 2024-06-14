package com.robokassa.library.models

data class PayActionState(val success: Boolean) : RoboApiResponse() {

    companion object {
        fun parse(src: String?): PayActionState {
            return PayActionState(src?.contains("success:true") == true)
        }
    }

}

data class PayRecurrentState(val success: Boolean) : RoboApiResponse() {

    companion object {
        fun parse(src: String?, invoiceId: Int): PayRecurrentState {
            return PayRecurrentState(src?.contains("OK$invoiceId") == true)
        }
    }

}

data object PayActionIdle : RoboApiResponse()
