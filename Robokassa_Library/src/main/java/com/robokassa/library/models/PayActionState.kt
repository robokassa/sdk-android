package com.robokassa.library.models

data class PayActionState(val success: Boolean) : RoboApiResponse() {

    companion object {
        fun parse(src: String?): PayActionState {
            return PayActionState(src?.contains("true") == true)
        }
    }

}

data object PayActionIdle : RoboApiResponse()
