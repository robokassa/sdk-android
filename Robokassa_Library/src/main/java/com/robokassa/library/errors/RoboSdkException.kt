package com.robokassa.library.errors

class RoboSdkException(throwable: Throwable, paymentId: Long? = null) : RuntimeException(throwable.message, throwable)