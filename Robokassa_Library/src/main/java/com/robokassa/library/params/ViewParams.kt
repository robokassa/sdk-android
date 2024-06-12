package com.robokassa.library.params

import android.graphics.Color
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import com.robokassa.library.errors.RoboSdkException

class ViewParams() : Params(), Parcelable {

    var toolbarBgColor: String? = null

    var toolbarTextColor: String? = null

    var toolbarText: String? = null

    var hasToolbar: Boolean = true

    private constructor(parcel: Parcel) : this() {
        parcel.run {
            toolbarBgColor = readString()
            toolbarTextColor = readString()
            toolbarText = readString()
            hasToolbar = readByte().toInt() != 0
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.run {
            writeString(toolbarBgColor)
            writeString(toolbarTextColor)
            writeString(toolbarText)
            writeByte((if (hasToolbar) 1 else 0).toByte())
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    @Throws(RoboSdkException::class)
    override fun checkRequiredFields() {
        check(toolbarBgColor.isNullOrEmpty() || try {
            Color.parseColor(toolbarBgColor)
            true
        } catch (e: Exception) {
            false
        }) { "Color has invalid format" }
        check(toolbarTextColor.isNullOrEmpty() || try {
            Color.parseColor(toolbarTextColor)
            true
        } catch (e: Exception) {
            false
        }) { "Color has invalid format" }
    }

    companion object CREATOR : Parcelable.Creator<ViewParams> {
        override fun createFromParcel(parcel: Parcel): ViewParams {
            return ViewParams(parcel)
        }

        override fun newArray(size: Int): Array<ViewParams?> {
            return arrayOfNulls(size)
        }
    }

}