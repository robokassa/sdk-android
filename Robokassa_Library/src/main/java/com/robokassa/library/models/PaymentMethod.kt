package com.robokassa.library.models

import com.google.gson.annotations.SerializedName

/**
 * Признак способа расчёта.
 */
enum class PaymentMethod {
    /** Предоплата 100%. Полная предварительная оплата до момента передачи предмета расчёта. */
    @SerializedName("full_prepayment")
    FULL_PREPAYMENT,
    /** Предоплата. Частичная предварительная оплата до момента передачи предмета расчёта. */
    @SerializedName("prepayment")
    PREPAYMENT,
    /** Аванс. */
    @SerializedName("advance")
    ADVANCE,
    /** Полный расчёт. Полная оплата, в том числе с учетом аванса (предварительной оплаты) в момент передачи предмета расчёта. */
    @SerializedName("full_payment")
    FULL_PAYMENT,
    /** Частичный расчёт и кредит. Частичная оплата предмета расчёта в момент его передачи с последующей оплатой в кредит. */
    @SerializedName("partial_payment")
    PARTIAL_PAYMENT,
    /** Передача в кредит. Передача предмета расчёт без его оплаты в момент его передачи с последующей оплатой в кредит. */
    @SerializedName("credit")
    CREDIT,
    /** Оплата кредита. Оплата предмета расчёта после его передачи с оплатой в кредит (оплата кредита). */
    @SerializedName("credit_payment")
    CREDIT_PAYMENT
}