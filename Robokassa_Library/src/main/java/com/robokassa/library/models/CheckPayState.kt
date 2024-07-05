package com.robokassa.library.models

import com.gitlab.mvysny.konsumexml.konsumeXml
import com.robokassa.library.errors.RoboApiException
import com.robokassa.library.helper.Logger

/**
 * Объект с результатом обработки платежного окна Robokassa.
 * @property requestCode
 * @property stateCode
 * @property desc
 * @property error
 */
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
                CheckRequestCode.SUCCESS.code -> requestCode = CheckRequestCode.SUCCESS
                CheckRequestCode.SIGNATURE_ERROR.code -> requestCode = CheckRequestCode.SIGNATURE_ERROR
                CheckRequestCode.SHOP_ERROR.code -> requestCode = CheckRequestCode.SHOP_ERROR
                CheckRequestCode.INVOICE_ZERO_ERROR.code -> requestCode = CheckRequestCode.INVOICE_ZERO_ERROR
                CheckRequestCode.INVOICE_DOUBLE_ERROR.code -> requestCode = CheckRequestCode.INVOICE_DOUBLE_ERROR
                CheckRequestCode.SERVER_ERROR.code -> requestCode = CheckRequestCode.SERVER_ERROR
            }

            var stateCode = CheckPayStateCode.NOT_INITED
            when (stateCodeParse) {
                CheckPayStateCode.INITED_NOT_PAYED.code -> stateCode = CheckPayStateCode.INITED_NOT_PAYED
                CheckPayStateCode.CANCELLED_NOT_PAYED.code -> stateCode = CheckPayStateCode.CANCELLED_NOT_PAYED
                CheckPayStateCode.HOLD_SUCCESS.code -> stateCode = CheckPayStateCode.HOLD_SUCCESS
                CheckPayStateCode.PAYED_NOT_TRANSFERRED.code -> stateCode = CheckPayStateCode.PAYED_NOT_TRANSFERRED
                CheckPayStateCode.PAYMENT_PAYBACK.code -> stateCode = CheckPayStateCode.PAYMENT_PAYBACK
                CheckPayStateCode.PAYMENT_STOPPED.code -> stateCode = CheckPayStateCode.PAYMENT_STOPPED
                CheckPayStateCode.PAYMENT_SUCCESS.code -> stateCode = CheckPayStateCode.PAYMENT_SUCCESS
            }
            return CheckPayState(
                stateCode = stateCode,
                requestCode = requestCode,
                desc = descParse
            )
        }
    }

}
