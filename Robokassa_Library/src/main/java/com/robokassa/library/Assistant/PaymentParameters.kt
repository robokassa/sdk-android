package com.robokassa.library.Assistant

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.annotation.Keep
import com.robokassa.library.RobokassaReceipt.RobokassaReceipt
import kotlinx.parcelize.Parcelize
import java.util.Date

enum class Culture {
    ru,
    en
}

enum class OutSumCurrency {
    USD,
    EUR,
    KZT
}

enum class RobokassaPayMetod {
    SIMPLE,
    HOLDING,
    RECURSSIVE
}

@[Parcelize Keep SuppressLint("ParcelCreator")]

data class PaymentParameters
@[JvmOverloads Keep] constructor(
    @Keep val merchantLogin: String,
    @Keep val password_1: String,
    @Keep val password_2: String,
    @Keep val description: String,
    @Keep val outSum: Double,

    @Keep val invoceID: Number? = null,
    @Keep val incCurrLabel:String? = null,
    @Keep val title: String? = null,
    @Keep val culture: Culture? = null,
    @Keep val email: String? = null,
    @Keep val expirationDate: Date? = null,
    @Keep val userIp: String? = null,
    @Keep val receipt: RobokassaReceipt? = null,
    ): Parcelable