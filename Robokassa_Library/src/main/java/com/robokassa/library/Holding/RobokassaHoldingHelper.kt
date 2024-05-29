package com.robokassa.library.Holding

import android.util.Log
import com.google.gson.Gson
import com.robokassa.library.Assistant.PaymentParameters
import com.robokassa.library.Assistant.RobokassaMD5
import java.net.URLEncoder

data class RobokassaHoldingPayResult(
    val error: Int,
    val requestParams: String?
)

object RobokassaHoldingHelper {

    fun paramsHoldingPayComplete(paymentParameters: PaymentParameters, testMode: Boolean): RobokassaHoldingPayResult {
        var curParams: String = ""
        var crсParams: String = ""
        var error = 0

        if (paymentParameters.merchantLogin.isNotEmpty()) {
            curParams += "MerchantLogin=" + paymentParameters.merchantLogin
            crсParams += paymentParameters.merchantLogin
        }


        if (paymentParameters.outSum > 0) {
            val outSum = paymentParameters.outSum.toString()
            curParams += "&OutSum=$outSum"
            crсParams += ":$outSum"
        }

        if (paymentParameters.invoceID != null) {
            curParams += "&InvoiceId=" + paymentParameters.invoceID.toString()
            crсParams += ":" + paymentParameters.invoceID.toString()
        } else {
            crсParams += ":"
        }

        if (paymentParameters.receipt != null) {
            val gson = Gson()
            val json = gson.toJson(paymentParameters.receipt)
            val json_utf8 = URLEncoder.encode(json, "utf-8")
            curParams += "&Receipt=$json_utf8"
            crсParams += ":$json"
        }

        if (paymentParameters.password_1.isNotEmpty()) {
            crсParams += ":" + paymentParameters.password_1
        }

        if(testMode)
            curParams += "&IsTest=1"


        Log.v("TEST", "crc $crсParams");

        val signatureValue = RobokassaMD5.md5Hash(crсParams)

        curParams += "&SignatureValue=$signatureValue"


        return RobokassaHoldingPayResult(error, curParams)
    }


    fun paramsHoldingPayCancel(paymentParameters: PaymentParameters, testMode: Boolean): RobokassaHoldingPayResult {
            var curParams: String = ""
            var crсParams: String = ""
            var error = 0

            if (paymentParameters.merchantLogin.isNotEmpty()) {
                curParams += "MerchantLogin=" + paymentParameters.merchantLogin
                crсParams += paymentParameters.merchantLogin
            }

            if (paymentParameters.outSum > 0) {
                val outSum = paymentParameters.outSum.toString()
                curParams += "&OutSum=$outSum"
                crсParams += ":$outSum"
            }


            if (paymentParameters.invoceID != null) {
                curParams += "&InvoiceId=" + paymentParameters.invoceID.toString()
                crсParams += "::" + paymentParameters.invoceID.toString()
            } else {
                crсParams += ":"
            }



            if (paymentParameters.password_1.isNotEmpty()) {
                curParams += "&password_1=" + paymentParameters.password_1
                crсParams += ":" + paymentParameters.password_1
            }

            if(testMode)
                curParams += "&IsTest=1"


            Log.v("TEST", "crc $crсParams");

            val signatureValue = RobokassaMD5.md5Hash(crсParams)

            curParams += "&SignatureValue=$signatureValue"


            return RobokassaHoldingPayResult(error, curParams)
        }

}