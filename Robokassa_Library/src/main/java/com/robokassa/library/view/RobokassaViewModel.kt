package com.robokassa.library.view

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.robokassa.library.api.ApiClient
import com.robokassa.library.api.ApiMethod
import com.robokassa.library.errors.RoboApiException
import com.robokassa.library.helper.Logger
import com.robokassa.library.models.CheckPayState
import com.robokassa.library.models.CheckPayStateCode
import com.robokassa.library.models.CheckRequestCode
import com.robokassa.library.params.PaymentParams
import com.robokassa.library.syncServerTimeDefault
import com.robokassa.library.syncServerTimeoutDefault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.max

class RobokassaViewModel : ViewModel() {

    companion object {
        val logs = mutableListOf<String>()
    }

    private var job: Job? = null

    private var startTime = 0L
    private var regress = 0L

    private val _paymentState = MutableStateFlow(
        CheckPayState(CheckRequestCode.CHECKING, CheckPayStateCode.NOT_INITED)
    )
    val paymentState: StateFlow<CheckPayState> = _paymentState.asStateFlow()

    fun initStatusTimer(paymentParams: PaymentParams) {
        startTime = System.currentTimeMillis()
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            request(paymentParams)
        }
    }

    fun stopStatusTimer(ctx: Context) {
        job?.cancel()
        clearParams(ctx)
    }

    fun saveParams(ctx: Context, paymentParams: PaymentParams) {
        val prefs = ctx.getSharedPreferences("robokassa.pay.prefs", Context.MODE_PRIVATE)
        prefs.edit().also {
            it.putString("pay", Gson().toJson(paymentParams))
        }.apply()
    }

    private fun clearParams(ctx: Context) {
        val prefs = ctx.getSharedPreferences("robokassa.pay.prefs", Context.MODE_PRIVATE)
        prefs.edit().also {
            it.remove("pay")
        }.apply()
    }

    private suspend fun request(paymentParams: PaymentParams) {
        if ((System.currentTimeMillis() - startTime) < syncServerTimeoutDefault) {
            val client = ApiClient()
            val result = client.performSuspendRequest(paymentParams, ApiMethod.CHECK)
            if (result.isSuccess) {
                val check = result.getOrNull()
                Logger.i("Check raw result: $check")
                (check as? CheckPayState)?.let {
                    _paymentState.value = it
                } ?: run {
                    _paymentState.value = CheckPayState(CheckRequestCode.CHECKING, CheckPayStateCode.NOT_INITED)
                }
            } else {
                val error = result.exceptionOrNull()
                Logger.i("Check raw error: $error")
                (error as? RoboApiException)?.let {
                    _paymentState.value = CheckPayState(
                        it.response?.requestCode ?: CheckRequestCode.TIMEOUT_ERROR,
                        it.response?.stateCode ?: CheckPayStateCode.NOT_INITED,
                        error = it
                    )
                } ?: run {
                    _paymentState.value = CheckPayState(CheckRequestCode.TIMEOUT_ERROR, CheckPayStateCode.NOT_INITED)
                }
            }
            delay(syncServerTimeDefault + max(100000L, regress))
            request(paymentParams)
        } else {
            _paymentState.value = CheckPayState(CheckRequestCode.TIMEOUT_ERROR, CheckPayStateCode.NOT_INITED)
        }
    }

}