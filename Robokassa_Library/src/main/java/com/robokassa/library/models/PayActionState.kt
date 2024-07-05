package com.robokassa.library.models

/**
 * Объект с результатом обработки действия с захолдированным платежом.
 * @property success
 */
data class PayActionState(val success: Boolean) : RoboApiResponse() {

    companion object {
        fun parse(src: String?): PayActionState {
            return PayActionState(src?.contains("success:true") == true || src?.contains("success: true") == true)
        }
    }

}

/**
 * Объект с результатом обработки действия с рекуррентным платежом.
 * @property success
 */
data class PayRecurrentState(val success: Boolean) : RoboApiResponse() {

    companion object {
        fun parse(src: String?, invoiceId: Int): PayRecurrentState {
            return PayRecurrentState(src?.contains("OK$invoiceId") == true)
        }
    }

}

data object PayActionIdle : RoboApiResponse()
