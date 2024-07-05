package com.robokassa.library.api

interface Disposable {

    fun dispose()

    fun isDisposed(): Boolean
}