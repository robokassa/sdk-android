package com.robokassa.library.errors

import com.robokassa.library.models.CheckPayState

class RoboApiException : Exception {

    var response: CheckPayState? = null

    constructor(message: String, response: CheckPayState, cause: Throwable) : super(message, cause) {
        this.response = response
    }

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(message: String, response: CheckPayState) : super(message) {
        this.response = response
    }

    constructor(response: CheckPayState) : super("") {
        this.response = response
    }
}

fun Throwable?.asRoboApiException() = (this as? RoboApiException)

fun Exception.getErrorCodeIfApiError() : String? {
    val api = (this as? RoboApiException) ?: return null
    return api.response?.requestCode?.code
}