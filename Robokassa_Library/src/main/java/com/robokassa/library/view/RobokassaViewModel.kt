package com.robokassa.library.view

import androidx.lifecycle.ViewModel
import com.robokassa.library.models.CheckPayState
import com.robokassa.library.models.CheckPayStateCode
import com.robokassa.library.models.CheckRequestCode
import com.robokassa.library.params.PaymentParams
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RobokassaViewModel : ViewModel() {

    private var job: Job? = null

    private val _paymentState = MutableStateFlow(
        CheckPayState(CheckRequestCode.CHECKING, CheckPayStateCode.NOT_INITED)
    )
    val paymentState: StateFlow<CheckPayState> = _paymentState.asStateFlow()

    fun initStatusTimer(paymentParams: PaymentParams) {

    }

    fun stopStatusTimer() {
        job?.cancel()
    }

}