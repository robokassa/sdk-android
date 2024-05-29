package com.robokassa.library.SimplePay

import android.util.Log
import com.google.gson.Gson
import com.robokassa.library.Assistant.Culture
import com.robokassa.library.Assistant.PaymentParameters
import com.robokassa.library.Assistant.RobokassaDateHelper.toIsoString
import com.robokassa.library.Assistant.RobokassaEncodingHelper.utf8ToUnicode
import com.robokassa.library.Assistant.RobokassaMD5.md5Hash
import com.robokassa.library.Assistant.RobokassaPayMetod
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


data class RobokassaSimplePayResult(
    val error: Int,
    val requestParams: String?,
)

object RobokassaSimplePay {

    fun paramsSimplePay(paymentParameters: PaymentParameters, testMode: Boolean, robokassaPayMetod: RobokassaPayMetod): RobokassaSimplePayResult {
        var curParams: String = ""
        var crсParams: String = ""
        var error = 0

        if (paymentParameters.merchantLogin.isNotEmpty()) {
            curParams += "MerchantLogin=" + paymentParameters.merchantLogin
            crсParams += paymentParameters.merchantLogin
        }

        if (paymentParameters.description.isNotEmpty()) {
            curParams += "&Description=" + paymentParameters.description
        }

        if (paymentParameters.outSum > 0) {
            val outSum = paymentParameters.outSum.toString()
            curParams += "&OutSum=$outSum"
            crсParams += ":$outSum"
        }

        if (paymentParameters.invoceID!= null) {
            curParams += "&invoiceID=" + paymentParameters.invoceID.toString()
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

        if(robokassaPayMetod == RobokassaPayMetod.HOLDING){
            curParams += "&StepByStep=true"
            crсParams += ":" + "true"
        }

        if (paymentParameters.password_1.isNotEmpty()) {
            //curParams += "&password_1=" + paymentParameters.password_1
            crсParams += ":" + paymentParameters.password_1
        }

        if (paymentParameters.incCurrLabel?.isNotEmpty() == true) {
            curParams += "&IncCurrLabel=" + paymentParameters.incCurrLabel
        }

        if (paymentParameters.title?.isNotEmpty() == true) {
            curParams += "&Title=" + paymentParameters.incCurrLabel
        }

        if (paymentParameters.culture != null) {

            if (paymentParameters.culture == Culture.en) {
                curParams += "&Culture=en"
            }

            if (paymentParameters.culture == Culture.ru) {
                curParams += "&Culture=ru"
            }
        }

        if (paymentParameters.email?.isNotEmpty() == true) {
            curParams += "&Email=" + paymentParameters.email
        }

        if (paymentParameters.expirationDate != null) {
            curParams += "&ExpirationDate=" + paymentParameters.expirationDate.toIsoString()
        }

        if (paymentParameters.userIp?.isNotEmpty() == true) {
            curParams += "&UserIp=" + paymentParameters.userIp
        }

        if(testMode)
            curParams += "&IsTest=1"


        Log.v("TEST", "crc $crсParams");

        val signatureValue = md5Hash(crсParams)

        curParams += "&SignatureValue=$signatureValue"


        return RobokassaSimplePayResult(error, curParams)
    }


    fun paramsSimplePayResult(paymentParameters: PaymentParameters): RobokassaSimplePayResult {
        var curParams: String = ""
        var crсParams: String = ""
        var error = 0

        if (paymentParameters.merchantLogin.isNotEmpty()) {
            curParams += "MerchantLogin=" + paymentParameters.merchantLogin
            crсParams += paymentParameters.merchantLogin
        }

        if (paymentParameters.invoceID != null) {
            curParams += "&invoiceID=" + paymentParameters.invoceID.toString()
            crсParams += ":" + paymentParameters.invoceID.toString()
        } else {
            crсParams += ":"
        }

        crсParams += ":" + paymentParameters.password_2

        val signatureValue = md5Hash(crсParams)

        curParams += "&Signature=$signatureValue"

        return RobokassaSimplePayResult(error, curParams)

    }
}