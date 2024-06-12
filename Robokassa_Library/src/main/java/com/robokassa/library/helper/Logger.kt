package com.robokassa.library.helper

import android.util.Log
import com.robokassa.library.LOG_TAG

object Logger {

    var logEnabled = false

    fun v(src: String) {
        if (logEnabled) {
            Log.v(LOG_TAG, src)
        }
    }

    fun d(src: String) {
        if (logEnabled) {
            Log.d(LOG_TAG, src)
        }
    }

    fun e(src: String) {
        if (logEnabled) {
            Log.e(LOG_TAG, src)
        }
    }

    fun i(src: String) {
        if (logEnabled) {
            Log.i(LOG_TAG, src)
        }
    }
}