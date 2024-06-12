package com.robokassa.library.models

import com.gitlab.mvysny.konsumexml.konsumeXml

data class CheckPayState(
    val requestCode: CheckRequestCode,
    val stateCode: CheckPayStateCode
) : RoboApiResponse() {

    companion object {
        fun parse(src: String?): CheckPayState {
            var codeParse = ""
            var stateCodeParse = ""
            try {
                src?.konsumeXml()?.apply {
                    child("OperationStateResponse") {
                        child("Result") {
                            codeParse = childText("Code")
                        }
                        child("State") {
                            stateCodeParse = childText("Code")
                            skipContents()

                        }
                        skipContents()
                    }
                }
            } catch (e: Exception) {
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
                "60" -> stateCode = CheckPayStateCode.PAYBACK
                "80" -> stateCode = CheckPayStateCode.STOPPED
                "100" -> stateCode = CheckPayStateCode.SUCCESS
            }
            return CheckPayState(
                stateCode = stateCode,
                requestCode = requestCode
            )
        }
    }

}
