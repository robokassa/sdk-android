package com.robokassa.library.errors

class RoboSdkException(throwable: Throwable) : RuntimeException(throwable.message, throwable)