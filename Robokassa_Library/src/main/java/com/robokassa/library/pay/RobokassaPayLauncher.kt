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
import com.robokassa.library.view.RobokassaActivity
import com.robokassa.library.errors.RoboApiException
import com.robokassa.library.errors.asRoboApiException
import com.robokassa.library.models.CheckPayState
import com.robokassa.library.params.PaymentParams
import kotlinx.parcelize.Parcelize
import java.io.Serializable

object RobokassaPayLauncher {
    sealed class Result
    class Success(
        val invoiceId: Int?,
        val resultCode: String?,
        val stateCode: String?
    ) : Result()

    data object Canceled : Result()
    class Error(
        val error: Throwable?,
        val resultCode: String?,
        val stateCode: String?,
        val desc: String?
    ) : Result() {

        constructor(error: RoboApiException) : this(error, error.response?.requestCode?.code, error.response?.stateCode?.code, error.response?.desc)
    }

    @Parcelize
    class StartPay(
        val paymentParams: PaymentParams
    ) : Parcelable

    object Contract : ActivityResultContract<StartPay, Result>() {
        override fun createIntent(context: Context, input: StartPay) =
            RobokassaActivity.intent(input.paymentParams, context)

        override fun parseResult(resultCode: Int, intent: Intent?): Result = when (resultCode) {
            AppCompatActivity.RESULT_OK -> {
                checkNotNull(intent)
                Success(
                    intent.getIntExtra(EXTRA_INVOICE_ID, -1),
                    intent.getStringExtra(EXTRA_CODE_RESULT),
                    intent.getStringExtra(EXTRA_CODE_STATE)
                )
            }

            AppCompatActivity.RESULT_FIRST_USER -> {
                val th = intent.getError()
                val c = intent?.getStringExtra(EXTRA_CODE_RESULT)
                val s = intent?.getStringExtra(EXTRA_CODE_STATE)
                val d = intent?.getStringExtra(EXTRA_ERROR_DESC)
                Error(
                    th,
                    c ?: th?.asRoboApiException()?.response?.requestCode?.code,
                    s ?: th?.asRoboApiException()?.response?.stateCode?.code,
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