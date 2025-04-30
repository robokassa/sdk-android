package com.robokassa.library.helper

import android.util.Log
import com.robokassa.library.LOG_TAG
import com.robokassa.library.view.RobokassaViewModel

object Logger {

    var logEnabled = false
    var logCollectEnabled = true

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
            RobokassaViewModel.logs.add(src)
        }
    }
}