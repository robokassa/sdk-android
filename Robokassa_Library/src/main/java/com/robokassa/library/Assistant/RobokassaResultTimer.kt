package com.robokassa.library.Assistant

import android.os.CountDownTimer
import android.os.Parcelable
import android.util.Log
import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.konsumeXml
import com.robokassa.library.SimplePay.RobokassaSimplePay.paramsSimplePayResult
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


enum class RobokassaResultTimerCode (val value: Int) {
    CODE_0(0),
    CODE_1(1),
    CODE_2(2),
    CODE_3(3),
    CODE_4(4),
    CODE_1000(5),
    TIMEOUT(6),
    IN_PROCESS(7);

    fun fromInt(value: Int): RobokassaResultTimerCode {
        var r_code = RobokassaResultTimerCode.IN_PROCESS
        when (value) {
            0 -> r_code = RobokassaResultTimerCode.CODE_0
            1 -> r_code = RobokassaResultTimerCode.CODE_1
            2 -> r_code = RobokassaResultTimerCode.CODE_2
            3 -> r_code = RobokassaResultTimerCode.CODE_3
            4 -> r_code = RobokassaResultTimerCode.CODE_4
            5 -> r_code = RobokassaResultTimerCode.CODE_1000
            6 -> r_code = RobokassaResultTimerCode.TIMEOUT
            7 -> r_code = RobokassaResultTimerCode.IN_PROCESS

        }
        return r_code
    }
}

enum class RobokassaResultTimerState (val value: Int) {
    NO_ORDER(0),
    CODE_5(1),
    CODE_10(2),
    CODE_20(3),
    CODE_50(4),
    CODE_60(5),
    CODE_80(6),
    CODE_100(7);

    fun fromInt(value: Int): RobokassaResultTimerState {
        var r_code = RobokassaResultTimerState.NO_ORDER
        when (value) {
            0 -> r_code = RobokassaResultTimerState.NO_ORDER
            1 -> r_code = RobokassaResultTimerState.CODE_5
            2 -> r_code = RobokassaResultTimerState.CODE_10
            3 -> r_code = RobokassaResultTimerState.CODE_20
            4 -> r_code = RobokassaResultTimerState.CODE_50
            5 -> r_code = RobokassaResultTimerState.CODE_60
            6 -> r_code = RobokassaResultTimerState.CODE_80
            7 -> r_code = RobokassaResultTimerState.CODE_100



        }
        return r_code
    }
}

data class RobokassaAnswer(
    var code: RobokassaResultTimerCode,
    var stateCode: RobokassaResultTimerState,
    var state: Boolean = false
)

interface RobokassaResultTimerCallback {
    fun result(robokassaAnswer: RobokassaAnswer)
}

class RobokassaResultTimer {
    lateinit var timer: CountDownTimer
    fun initTimer(paymentParameters: PaymentParameters, url: String,syncTime: Long, syncTimeout : Long, result: RobokassaResultTimerCallback){
        timer = object: CountDownTimer(syncTimeout, syncTime) {
            override fun onTick(millisUntilFinished: Long) {

                val finalUrl = url + "?" + paramsSimplePayResult(paymentParameters).requestParams

                val client = OkHttpClient()


                val request = Request.Builder()
                    .url(finalUrl)
                    .build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (response.isSuccessful) {
                                val answer = parseResponse(response.body!!.string())
                                if( answer.code !== RobokassaResultTimerCode.IN_PROCESS)
                                    result.result(answer)
                            }
                        }
                    }
                })

            }
            override fun onFinish() {
                result.result(RobokassaAnswer( RobokassaResultTimerCode.TIMEOUT, RobokassaResultTimerState.NO_ORDER))
            }
        }
        timer.start()
    }

    private fun parseResponse(xmlStr:String):RobokassaAnswer {
        var codeParse: String = ""
        var stateCodeParse: String = ""
        try {
            xmlStr.konsumeXml().apply {
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
        } catch (_: Exception) {
            return RobokassaAnswer(
                RobokassaResultTimerCode.IN_PROCESS,
                RobokassaResultTimerState.NO_ORDER
            )
        }

        var r_code = RobokassaResultTimerCode.IN_PROCESS

        when (codeParse) {
            "0" -> r_code = RobokassaResultTimerCode.CODE_0
            "2" -> r_code = RobokassaResultTimerCode.CODE_2
            "3" -> r_code = RobokassaResultTimerCode.CODE_3
            "4" -> r_code = RobokassaResultTimerCode.CODE_4
            "1000" -> r_code = RobokassaResultTimerCode.CODE_1000
        }

        var r_state = RobokassaResultTimerState.NO_ORDER
        when (stateCodeParse) {
            "5" -> r_state = RobokassaResultTimerState.CODE_5
            "10" -> r_state = RobokassaResultTimerState.CODE_10
            "20" -> r_state = RobokassaResultTimerState.CODE_20
            "60" -> r_state = RobokassaResultTimerState.CODE_60
            "80" -> r_state = RobokassaResultTimerState.CODE_80
            "100" -> r_state = RobokassaResultTimerState.CODE_100
        }
        return RobokassaAnswer(code = r_code, stateCode = r_state)
    }

    fun destroy(){
        timer.cancel()
    }

}