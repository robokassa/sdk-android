package com.robokassa.library.Holding

import android.content.Context
import com.robokassa.library.Assistant.PaymentParameters
import com.robokassa.library.Assistant.RobokassaAnswer
import com.robokassa.library.Assistant.RobokassaResultCallBack
import com.robokassa.library.Assistant.RobokassaResultTimerCode
import com.robokassa.library.Assistant.RobokassaResultTimerState
import com.robokassa.library.Holding.RobokassaHoldingHelper.paramsHoldingPayCancel
import com.robokassa.library.Holding.RobokassaHoldingHelper.paramsHoldingPayComplete
import com.robokassa.library.urlHoldingConfirm
import okhttp3.Call
import okhttp3.Callback
import okhttp3.ConnectionSpec
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.conscrypt.Conscrypt
import java.io.IOException
import java.security.Security
import java.util.Arrays


object RobokassaHolding {

    fun robokassaHoldingComplete(
        context: Context,
        paymentParameters: PaymentParameters,
        testParameters: Boolean = false,
        result: RobokassaResultCallBack?,
    ){

        val client = OkHttpClient.Builder()
           // .
           // .connectionSpecs(
             //   listOf(
               //     ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS,ConnectionSpec.RESTRICTED_TLS,ConnectionSpec.CLEARTEXT)
              //  )
            .build()

        val type = "application/x-www-form-urlencoded; charset=utf-8".toMediaType()

        val request = Request.Builder()
            .url(urlHoldingConfirm)
            .post(paramsHoldingPayComplete(paymentParameters,testParameters).requestParams!!.toRequestBody(type))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                result?.result(RobokassaAnswer(RobokassaResultTimerCode.TIMEOUT,RobokassaResultTimerState.CODE_80))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                            result?.result(RobokassaAnswer(RobokassaResultTimerCode.CODE_0,RobokassaResultTimerState.CODE_5,true))
                    }
                }
            }
        })
    }


    fun robokassaHoldingCancel(
        context: Context,
        paymentParameters: PaymentParameters,
        testParameters: Boolean = false,
        result: RobokassaResultCallBack?,
    ){
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(urlHoldingConfirm)
            .post(paramsHoldingPayCancel(paymentParameters,testParameters).requestParams!!.toRequestBody())
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                result?.result(RobokassaAnswer(RobokassaResultTimerCode.TIMEOUT,RobokassaResultTimerState.CODE_80))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        result?.result(RobokassaAnswer(RobokassaResultTimerCode.CODE_0,RobokassaResultTimerState.CODE_5,true))
                    }
                }
            }
        })
    }
}