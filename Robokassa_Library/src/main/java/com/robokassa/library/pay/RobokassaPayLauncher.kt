package com.robokassa.library.pay

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import com.robokassa.library.EXTRA_CODE_RESULT
import com.robokassa.library.EXTRA_CODE_STATE
import com.robokassa.library.EXTRA_ERROR
import com.robokassa.library.EXTRA_ERROR_DESC
import com.robokassa.library.EXTRA_INVOICE_ID
import com.robokassa.library.EXTRA_OP_KEY
import com.robokassa.library.view.RobokassaActivity
import com.robokassa.library.errors.RoboApiException
import com.robokassa.library.errors.asRoboApiException
import com.robokassa.library.models.CheckPayStateCode
import com.robokassa.library.models.CheckRequestCode
import com.robokassa.library.params.PaymentParams
import kotlinx.parcelize.Parcelize
import java.io.Serializable

object RobokassaPayLauncher {
    sealed class Result

    /**
     * Объект успешного возврата из окна оплаты Robokassa.
     * @property invoiceId Номер успешно оплаченного заказа
     * @property opKey Идентификатор операции (нужен для оплаты по сохраненной карте)
     * @property resultCode
     * @property stateCode
     */
    class Success(
        val invoiceId: Int?,
        val resultCode: CheckRequestCode?,
        val stateCode: CheckPayStateCode?,
        val opKey: String?
    ) : Result()

    data object Canceled : Result()

    /**
     * Объект возврата с ошибкой из окна оплаты Robokassa.
     * @property error
     * @property resultCode
     * @property stateCode
     * @property desc
     */
    class Error(
        val error: Throwable?,
        val resultCode: CheckRequestCode?,
        val stateCode: CheckPayStateCode?,
        val desc: String?
    ) : Result() {

        constructor(error: RoboApiException) : this(error, error.response?.requestCode, error.response?.stateCode, error.response?.desc)
    }

    /**
     * Объект для запуска окна оплаты Robokassa.
     * @property paymentParams
     */
    @Parcelize
    class StartPay(
        val paymentParams: PaymentParams,
        val testMode: Boolean = false
    ) : Parcelable

    object Contract : ActivityResultContract<StartPay, Result>() {
        override fun createIntent(context: Context, input: StartPay) =
            RobokassaActivity.intent(input.paymentParams, input.testMode, context)

        override fun parseResult(resultCode: Int, intent: Intent?): Result = when (resultCode) {
            AppCompatActivity.RESULT_OK -> {
                checkNotNull(intent)
                Success(
                    intent.getIntExtra(EXTRA_INVOICE_ID, -1),
                    intent.serializable(EXTRA_CODE_RESULT),
                    intent.serializable(EXTRA_CODE_STATE),
                    intent.getStringExtra(EXTRA_OP_KEY)
                )
            }

            AppCompatActivity.RESULT_FIRST_USER -> {
                val th = intent.getError()
                val c: CheckRequestCode? = intent?.serializable(EXTRA_CODE_RESULT)
                val s: CheckPayStateCode? = intent?.serializable(EXTRA_CODE_STATE)
                val d = intent?.getStringExtra(EXTRA_ERROR_DESC)
                Error(
                    th,
                    c ?: th?.asRoboApiException()?.response?.requestCode,
                    s ?: th?.asRoboApiException()?.response?.stateCode,
                    d ?: th?.asRoboApiException()?.response?.desc
                )
            }

            else -> Canceled
        }

    }

    fun Intent?.getError(): Throwable? {
        return this?.serializable(EXTRA_ERROR)
    }

    private inline fun <reified T : Serializable> Intent?.serializable(key: String): T? = when {
        this == null -> null
        Build.VERSION.SDK_INT >= 33 -> getSerializableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
    }

}