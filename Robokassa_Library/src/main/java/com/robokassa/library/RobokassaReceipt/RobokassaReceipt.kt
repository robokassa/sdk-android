package com.robokassa.library.RobokassaReceipt

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

enum class ReceiptSNO (val value: String) {
    osn("osn"),
    usn_income("usn_income"),
    usn_income_outcome("usn_income_outcome"),
    esn("esn"),
    patent("patent")
}

enum class ReceiptPaymentMethod (val value: String) {
    full_prepayment("full_prepayment"),
    prepayment("prepayment"),
    advance("advance"),
    full_payment("full_payment"),
    partial_payment("partial_payment"),
    credit("credit"),
    credit_payment("credit_payment")
}

enum class ReceiptPaymentObject (val value: String) {
    commodity("commodity"),
    excise("excise"),
    job("job"),
    service("service"),
    gambling_bet("gambling_bet"),
    gambling_prize("gambling_prize"),
    lottery("lottery"),
    lottery_prize("lottery_prize"),
    intellectual_activity("intellectual_activity"),
    payment("payment"),
    agent_commission("agent_commission"),
    composite("composite"),
    resort_fee("resort_fee"),
    another("another"),
    property_right("property_right"),
    non_operating_gain("non-operating_gain"),
    insurance_premium("insurance_premium"),
    sales_tax("sales_tax")


}

enum class ReceiptTax (val value: String) {
    none("none"),
    vat0("vat0"),
    vat10("vat10"),
    vat110("vat110"),
    vat20("vat20"),
    vat120("vat120")
}

@[Parcelize Keep SuppressLint("ParcelCreator")]

data class RobokassaReceiptItem
@[JvmOverloads Keep] constructor(
    @Keep var name: String,
    @Keep val sum: Double,
    @Keep val coast: Double? = null,
    @Keep val quantity: Int,
    @Keep val payment_method: ReceiptPaymentMethod? = null,
    @Keep val payment_object: ReceiptPaymentObject? = null,
    @Keep val tax: ReceiptTax,
    @Keep val nomenclature_code: String? = null
): Parcelable

@[Parcelize Keep SuppressLint("ParcelCreator")]

data class RobokassaReceipt
@[JvmOverloads Keep] constructor(
    @Keep val sno: ReceiptSNO? = null,
    @Keep val items: Array <RobokassaReceiptItem>,


    ): Parcelable {

        override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RobokassaReceipt

        if (sno != other.sno) return false
        if (!items.contentEquals(other.items)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sno?.hashCode() ?: 0
        result = 31 * result + items.contentHashCode()
        return result
    }
}