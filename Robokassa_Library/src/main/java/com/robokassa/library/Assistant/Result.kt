package com.robokassa.library.Assistant

import androidx.annotation.Keep
import java.io.Serializable

@Keep

interface RobokassaResultCallBack {
    fun result(robokassaResult: RobokassaAnswer)
}
