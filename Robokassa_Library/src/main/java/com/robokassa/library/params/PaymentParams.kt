package com.robokassa.library.params

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import com.robokassa.library.errors.RoboSdkException

class PaymentParams() : BaseParams(), Parcelable {

    lateinit var order: OrderParams

    var customer: CustomerParams = CustomerParams()

    var view: ViewParams = ViewParams()

    @Suppress("DEPRECATION")
    private constructor(parcel: Parcel) : this() {
        parcel.run {
            setCredentials(
                merchantLogin = readString() ?: "",
                password1 = readString() ?: "",
                password2 = readString() ?: ""
            )
            order = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                readParcelable(OrderParams::class.java.classLoader, OrderParams::class.java)!!
            } else {
                readParcelable(OrderParams::class.java.classLoader)!!
            }
            customer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                readParcelable(CustomerParams::class.java.classLoader, CustomerParams::class.java)!!
            } else {
                readParcelable(CustomerParams::class.java.classLoader)!!
            }
            view = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                readParcelable(ViewParams::class.java.classLoader, ViewParams::class.java)!!
            } else {
                readParcelable(ViewParams::class.java.classLoader)!!
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.run {
            writeString(merchantLogin)
            writeString(password1)
            writeString(password2)
            writeParcelable(order, flags)
            writeParcelable(customer, flags)
            writeParcelable(view, flags)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    @Throws(RoboSdkException::class)
    override fun checkRequiredFields() {
        super.checkRequiredFields()
        kotlin.check(::order.isInitialized) { "Order Params is not set" }
        order.checkRequiredFields()
        customer.checkRequiredFields()
        view.checkRequiredFields()
    }

    fun setParams(options: PaymentParams.() -> Unit): PaymentParams {
        return PaymentParams().apply(options)
    }

    fun orderParams(orderParams: OrderParams.() -> Unit) {
        this.order = OrderParams().apply(orderParams)
    }

    fun customerParams(customerParams: CustomerParams.() -> Unit) {
        this.customer = CustomerParams().apply(customerParams)
    }

    fun viewParams(viewParams: ViewParams.() -> Unit) {
        this.view = ViewParams().apply(viewParams)
    }

    companion object CREATOR : Parcelable.Creator<PaymentParams> {

        override fun createFromParcel(parcel: Parcel): PaymentParams {
            return PaymentParams(parcel)
        }

        override fun newArray(size: Int): Array<PaymentParams?> {
            return arrayOfNulls(size)
        }
    }
}