package com.robokassa.library.params

import android.os.Parcel
import android.os.Parcelable

import com.robokassa.library.errors.RoboSdkException

open class BaseParams() : Params(), Parcelable {

    /**
     * Идентификатор магазина.
     */
    lateinit var merchantLogin: String
        private set

    /**
     * Пароль#1 из личного кабинета Robokassa.
     */
    lateinit var password1: String
        private set

    /**
     * Пароль#2 из личного кабинета Robokassa.
     */
    lateinit var password2: String
        private set

    private constructor(parcel: Parcel) : this() {
        parcel.run {
            merchantLogin = readString() ?: ""
            password1 = readString() ?: ""
            password2 = readString() ?: ""
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.run {
            writeString(merchantLogin)
            writeString(password1)
            writeString(password2)
        }
    }

    @Throws(RoboSdkException::class)
    override fun checkRequiredFields() {
        check(merchantLogin.isNotEmpty()) { "Merchant Login should not be empty" }
        check(password1.isNotEmpty()) { "Password 1 should not be empty" }
        check(password2.isNotEmpty()) { "Password 2 should not be empty" }
    }

    fun setCredentials(merchantLogin: String, password1: String, password2: String) {
        this.merchantLogin = merchantLogin
        this.password1 = password1
        this.password2 = password2
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BaseParams> {
        override fun createFromParcel(parcel: Parcel): BaseParams {
            return BaseParams(parcel)
        }

        override fun newArray(size: Int): Array<BaseParams?> {
            return arrayOfNulls(size)
        }
    }
}

