package com.robokassa.library.errors

internal class RoboSdkException(throwable: Throwable) : RuntimeException(throwable.message, throwable)