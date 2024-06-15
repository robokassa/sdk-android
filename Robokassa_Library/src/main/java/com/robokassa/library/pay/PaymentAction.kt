package com.robokassa.library.pay

import com.robokassa.library.api.ApiClient
import com.robokassa.library.api.ApiMethod
import com.robokassa.library.helper.CoroutineManager
import com.robokassa.library.models.PayActionIdle
import com.robokassa.library.models.PayActionState
import com.robokassa.library.models.PayRecurrentState
import com.robokassa.library.models.RoboApiResponse
import com.robokassa.library.params.PaymentParams
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Класс для работы с платежами
 */
class PaymentAction internal constructor(
    private val api: ApiClient = ApiClient(),
    private val manager: CoroutineManager = CoroutineManager()
) {

    private val _state = MutableStateFlow<RoboApiResponse>(PayActionIdle)
    val state = _state

    /**
     * Метод запуска подтверждения предварительно захолдированного платежа.
     * @param params Параметры платежа
     */
    fun confirmHold(params: PaymentParams) {
        _state.value = PayActionIdle
        manager.launchOnBackground {
            try {
                _state.value = (api.performSuspendRequest(params, ApiMethod.HOLD_CONFIRM).getOrNull() as? PayActionState) ?: PayActionState(false)
            } catch (e: Throwable) {
                _state.value = PayActionState(false)
            }
        }
    }

    /**
     * Метод запуска отмены предварительно захолдированного платежа.
     * @param params Параметры платежа
     */
    fun cancelHold(params: PaymentParams) {
        _state.value = PayActionIdle
        manager.launchOnBackground {
            try {
                _state.value = (api.performSuspendRequest(params, ApiMethod.HOLD_CANCEL).getOrNull() as? PayActionState) ?: PayActionState(false)
            } catch (e: Throwable) {
                _state.value = PayActionState(false)
            }
        }
    }

    /**
     * Метод запуска рекуррентного (повторного) платежа.
     * @param params Параметры платежа
     */
    fun payRecurrent(params: PaymentParams) {
        _state.value = PayActionIdle
        manager.launchOnBackground {
            try {
                _state.value = (api.performSuspendRequest(params, ApiMethod.RECURRENT).getOrNull() as? PayRecurrentState) ?: PayRecurrentState(false)
            } catch (e: Throwable) {
                _state.value = PayRecurrentState(false)
            }
        }
    }

    companion object {
        fun init() = PaymentAction()
    }

}