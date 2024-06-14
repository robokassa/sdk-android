package com.robokassa.library.models

import com.gitlab.mvysny.konsumexml.konsumeXml
import com.robokassa.library.errors.RoboApiException
import com.robokassa.library.helper.Logger

data class CheckPayState(
    val requestCode: CheckRequestCode,
    val stateCode: CheckPayStateCode,
    val desc: String? = null,
    val error: RoboApiException? = null
) : RoboApiResponse() {

    companion object {
        fun parse(src: String?): CheckPayState {
            var codeParse = ""
            var stateCodeParse = ""
            var descParse = ""
            try {
                src?.konsumeXml()?.apply {
                    child("OperationStateResponse") {
                        childOrNull("Result") {
                            codeParse = childTextOrNull("Code") ?: ""
                            descParse = childTextOrNull("Description") ?: ""
                        }
                        childOrNull("State") {
                            stateCodeParse = childTextOrNull("Code") ?: ""
                            skipContents()
                        }
                        skipContents()
                    }
                }
            } catch (e: Exception) {
                Logger.e("Check parse error $e")
                return CheckPayState(
                    stateCode = CheckPayStateCode.NOT_INITED,
                    requestCode = CheckRequestCode.CHECKING
                )
            }
            var requestCode = CheckRequestCode.CHECKING

            when (codeParse) {
                "0" -> requestCode = CheckRequestCode.SUCCESS
                "1" -> requestCode = CheckRequestCode.SIGNATURE_ERROR
                "2" -> requestCode = CheckRequestCode.SHOP_ERROR
                "3" -> requestCode = CheckRequestCode.INVOICE_ZERO_ERROR
                "4" -> requestCode = CheckRequestCode.INVOICE_DOUBLE_ERROR
                "1000" -> requestCode = CheckRequestCode.SERVER_ERROR
            }

            var stateCode = CheckPayStateCode.NOT_INITED
            when (stateCodeParse) {
                "5" -> stateCode = CheckPayStateCode.INITED_NOT_PAYED
                "10" -> stateCode = CheckPayStateCode.CANCELLED_NOT_PAYED
                "20" -> stateCode = CheckPayStateCode.HOLD_SUCCESS
                "50" -> stateCode = CheckPayStateCode.PAYED_NOT_TRANSFERRED
                "60" -> stateCode = CheckPayStateCode.PAYMENT_PAYBACK
                "80" -> stateCode = CheckPayStateCode.PAYMENT_STOPPED
                "100" -> stateCode = CheckPayStateCode.PAYMENT_SUCCESS
            }
            return CheckPayState(
                stateCode = stateCode,
                requestCode = requestCode,
                desc = descParse
            )
        }
    }

}
