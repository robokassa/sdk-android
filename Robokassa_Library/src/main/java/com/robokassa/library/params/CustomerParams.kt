package com.robokassa.library.params

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import com.robokassa.library.errors.RoboSdkException
import com.robokassa.library.models.Culture

/**
 * Объект данных о покупателе.
 */
class CustomerParams() : Params(), Parcelable {

    /**
     *
     * Язык общения с клиентом (в соответствии с ISO 3166-1). Определяет на каком языке будет страница
     * Robokassa, на которую попадёт покупатель. Если параметр не передан, то используются региональные
     * настройки браузера покупателя. Для значений отличных от ru или en используется английский язык.
     */
    var culture: Culture? = null

    /**
     * Если параметр передан, то email покупателя автоматически подставляется в платёжную форму Robokassa.
     */
    var email: String? = null

    /**
     * Передача этого параметра (Ip конечного пользователя) желательна для усиления безопасности,
     * предотвращению фрода и противодействию мошенникам.
     */
    var ip: String? = null

    @Suppress("DEPRECATION")
    private constructor(parcel: Parcel) : this() {
        parcel.run {
            culture = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                readSerializable(Culture::class.java.classLoader, Culture::class.java)
            } else {
                readSerializable() as? Culture
            }
            email = readString()
            ip = readString()
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.run {
            writeSerializable(culture)
            writeString(email)
            writeString(ip)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    @Throws(RoboSdkException::class)
    override fun checkRequiredFields() {
        check(email.isNullOrEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { "Email has invalid format" }
    }

    companion object CREATOR : Parcelable.Creator<CustomerParams> {
        override fun createFromParcel(parcel: Parcel): CustomerParams {
            return CustomerParams(parcel)
        }

        override fun newArray(size: Int): Array<CustomerParams?> {
            return arrayOfNulls(size)
        }
    }

}