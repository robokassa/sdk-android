package com.robokassa.library

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.gun0912.tedonactivityresult.TedOnActivityResult
import com.robokassa.library.Assistant.PaymentParameters
import com.robokassa.library.Assistant.RobokassaAnswer
import com.robokassa.library.Assistant.RobokassaPayMetod
import com.robokassa.library.Assistant.RobokassaResultCallBack
import com.robokassa.library.Assistant.RobokassaResultTimerCode
import com.robokassa.library.Assistant.RobokassaResultTimerState
import com.robokassa.library.Holding.RobokassaHolding.robokassaHoldingCancel
import com.robokassa.library.Holding.RobokassaHolding.robokassaHoldingComplete
import com.robokassa.library.SimplePay.RobokassaActivity


object Robokassa {

    @JvmOverloads
    @JvmStatic
    fun createSimplePay(
        context: Context,
        paymentParameters: PaymentParameters,
        testParameters: Boolean = false,
        syncServerTime: Long = syncServerTimeDefault,
        syncServerTimeout: Long = syncServerTimeoutDefault,
        result: RobokassaResultCallBack,

        ): Intent {
        val intent = Intent(context, RobokassaActivity::class.java)
            .putExtra(EXTRA_PAYMENT_PARAMETERS, paymentParameters)
            .putExtra(EXTRA_TEST_PARAMETERS, testParameters)
            .putExtra(EXTRA_METOD_PARAMETERS, RobokassaPayMetod.SIMPLE)
            .putExtra(EXTRA_METOD_SYNC_SERVER_TIME, syncServerTime)
            .putExtra(EXTRA_METOD_SYNC_SERVER_TIMEOUT,syncServerTimeout)

        TedOnActivityResult.with(context)
            .setIntent(intent)
            .setListener { resultCode: Int, data: Intent ->
                if (resultCode == Activity.RESULT_OK) {
                    val r_code = RobokassaResultTimerCode.CODE_2.fromInt(data.getIntExtra(EXTRA_METOD_CODE,0))
                    val r_code_state  = RobokassaResultTimerState.NO_ORDER.fromInt(data.getIntExtra(EXTRA_METOD_CODE_STATE,0))
                    result.result(RobokassaAnswer(r_code,r_code_state))
                }
            }
            .startActivityForResult()
        return intent
    }

    @JvmOverloads
    @JvmStatic
    fun createHoldingPay(
        context: Context,
        paymentParameters: PaymentParameters,
        testParameters: Boolean = false,
        syncServerTime: Long = syncServerTimeDefault,
        syncServerTimeout: Long = syncServerTimeoutDefault,
        result: RobokassaResultCallBack,

        ): Intent {
        val intent = Intent(context, RobokassaActivity::class.java)
            .putExtra(EXTRA_PAYMENT_PARAMETERS, paymentParameters)
            .putExtra(EXTRA_TEST_PARAMETERS, testParameters)
            .putExtra(EXTRA_METOD_PARAMETERS, RobokassaPayMetod.HOLDING)
            .putExtra(EXTRA_METOD_SYNC_SERVER_TIME, syncServerTime)
            .putExtra(EXTRA_METOD_SYNC_SERVER_TIMEOUT,syncServerTimeout)

        TedOnActivityResult.with(context)
            .setIntent(intent)
            .setListener { resultCode: Int, data: Intent ->
                if (resultCode == Activity.RESULT_OK) {
                    val r_code = RobokassaResultTimerCode.CODE_2.fromInt(data.getIntExtra(EXTRA_METOD_CODE,0))
                    val r_code_state  = RobokassaResultTimerState.NO_ORDER.fromInt(data.getIntExtra(EXTRA_METOD_CODE_STATE,0))
                    result.result(RobokassaAnswer(r_code,r_code_state))
                }
            }
            .startActivityForResult()
        return intent
    }

    fun holdingComplete(
        context: Context,
        paymentParameters: PaymentParameters,
        testParameters: Boolean = false,
        result: RobokassaResultCallBack?
    ){
        robokassaHoldingComplete(context,
            paymentParameters,
            testParameters,
            result)



    }

    fun holdingCancel(
        context: Context,
        paymentParameters: PaymentParameters,
        testParameters: Boolean = false,
        result: RobokassaResultCallBack?
    ){
        robokassaHoldingCancel(context,
            paymentParameters,
            testParameters,
            result)

    }
}



