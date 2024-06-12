package com.robokassa.library.params

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import com.robokassa.library.errors.RoboSdkException
import com.robokassa.library.models.Currency
import com.robokassa.library.models.Receipt
import java.util.Date

class OrderParams() : Params(), Parcelable {

    var invoiceId: Int = -1

    var previousInvoiceId: Int = -1

    var orderSum: Double = 0.0

    var description: String? = null

    var incCurrLabel: String? = null

    var isRecurrent: Boolean = false

    var isHold: Boolean = false

    var outSumCurrency: Currency? = null

    var expirationDate: Date? = null

    var receipt: Receipt? = null

    @Suppress("DEPRECATION")
    private constructor(parcel: Parcel) : this() {
        parcel.run {
            invoiceId = readInt()
            previousInvoiceId = readInt()
            orderSum = readDouble()
            description = readString()
            incCurrLabel = readString()
            isRecurrent = readByte().toInt() != 0
            isHold = readByte().toInt() != 0
            outSumCurrency = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                readSerializable(Currency::class.java.classLoader, Currency::class.java)
            } else {
                readSerializable() as? Currency
            }
            expirationDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                readSerializable(Date::class.java.classLoader, Date::class.java)
            } else {
                readSerializable() as? Date
            }
            receipt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                readSerializable(Receipt::class.java.classLoader, Receipt::class.java)
            } else {
                readSerializable() as? Receipt
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.run {
            writeInt(invoiceId)
            writeInt(previousInvoiceId)
            writeDouble(orderSum)
            writeString(description)
            writeString(incCurrLabel)
            writeByte((if (isRecurrent) 1 else 0).toByte())
            writeByte((if (isHold) 1 else 0).toByte())
            writeSerializable(outSumCurrency)
            writeSerializable(expirationDate)
            writeSerializable(receipt)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    @Throws(RoboSdkException::class)
    override fun checkRequiredFields() {
        check(orderSum > 0.0) { "Order Sum value cannot be less than 0" }
        check((description?.length ?: 0) > 100) { "Description value cannot be 100 chars longer" }
    }

    companion object CREATOR : Parcelable.Creator<OrderParams> {
        override fun createFromParcel(parcel: Parcel): OrderParams {
            return OrderParams(parcel)
        }

        override fun newArray(size: Int): Array<OrderParams?> {
            return arrayOfNulls(size)
        }
    }
}