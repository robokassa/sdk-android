package com.robokassa.library.params

import com.robokassa.library.errors.RoboSdkException

abstract class Params {

    internal abstract fun checkRequiredFields()

    @Throws(RoboSdkException::class)
    internal fun check(condition: Boolean, lazyMessage: () -> Any) {
        if (!condition) {
            val message = lazyMessage()
            throw RoboSdkException(IllegalStateException(message.toString()))
        }
    }
}