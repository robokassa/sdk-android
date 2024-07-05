package com.robokassa.library.api

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface ApiRequest<R> : Disposable {

    fun execute(onSuccess: (R) -> Unit, onFailure: (Exception) -> Unit)

    suspend fun execute(): R = suspendCoroutine { c ->
        execute(onSuccess = { c.resume(it) }, onFailure = { c.resumeWithException(it) })
    }
}