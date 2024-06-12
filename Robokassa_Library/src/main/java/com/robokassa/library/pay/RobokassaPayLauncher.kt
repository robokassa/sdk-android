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
        val error: Throwable,
        val errorCode: CheckPayState?
    ) : Result() {

        constructor(error: RoboApiException) : this(error, error.response)
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
                Error(th, th.asRoboApiException()?.response)
            }
            else -> Canceled
        }

    }

    fun Intent?.getError(): Throwable {
        return checkNotNull(this?.serializable(EXTRA_ERROR))
    }

    inline fun <reified T : Serializable> Intent?.serializable(key: String): T? = when {
        this == null -> null
        Build.VERSION.SDK_INT >= 33 -> getSerializableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
    }

}