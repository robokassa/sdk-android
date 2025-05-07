package com.robokassa.library.helper

import android.util.Log
import com.robokassa.library.LOG_TAG
import com.robokassa.library.view.RobokassaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Logger {

    var logEnabled = false
    private var logCollectEnabled = false
    private val format = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.US)


    fun v(src: String) {
        if (logEnabled) {
            Log.v(LOG_TAG, src)
        }
        if (logCollectEnabled) {
            RobokassaViewModel.logs.add(src)
        }
    }

    fun d(src: String) {
        if (logEnabled) {
            Log.d(LOG_TAG, src)
        }
        if (logCollectEnabled) {
            RobokassaViewModel.logs.add(src)
        }
    }

    fun e(src: String) {
        if (logEnabled) {
            Log.e(LOG_TAG, src)
        }
        if (logCollectEnabled) {
            RobokassaViewModel.logs.add(src)
        }
    }

    fun i(src: String) {
        if (logEnabled) {
            Log.i(LOG_TAG, src)
        }
        if (logCollectEnabled) {
            RobokassaViewModel.logs.add("${format.format(Date())} - $src")
        }
    }
}