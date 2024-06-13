package com.robokassa.library.helper

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class CoroutineManager(
    private val exceptionHandler: (Throwable) -> Unit,
    val io: CoroutineDispatcher = Dispatchers.IO,
    val main: CoroutineDispatcher = Dispatchers.Main
) {

    constructor(
        io: CoroutineDispatcher = Dispatchers.IO,
        main: CoroutineDispatcher = Dispatchers.Main
    ) : this({}, io, main)

    private val job = SupervisorJob()
    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable -> launchOnMain { exceptionHandler(throwable) } }
    private val coroutineScope = CoroutineScope(Dispatchers.Main + coroutineExceptionHandler + job)

    fun launchOnMain(block: suspend CoroutineScope.() -> Unit): Job {
        return coroutineScope.launch(main) {
            block.invoke(this)
        }
    }

    suspend fun withMain(block: suspend CoroutineScope.() -> Unit) {
        withContext(main) {
            block.invoke(this)
        }
    }

    fun launchOnBackground(block: suspend CoroutineScope.() -> Unit): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            block.invoke(this)
        }
    }
}