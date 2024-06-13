package com.robokassa.library.api

import com.robokassa.library.FORM_URL_ENCODED
import com.robokassa.library.errors.RoboApiException
import com.robokassa.library.helper.Logger
import com.robokassa.library.helper.cancelHoldPostParams
import com.robokassa.library.helper.checkPostParams
import com.robokassa.library.helper.confirmHoldPostParams
import com.robokassa.library.models.CheckPayState
import com.robokassa.library.models.CheckPayStateCode
import com.robokassa.library.models.CheckRequestCode
import com.robokassa.library.models.PayActionState
import com.robokassa.library.models.RoboApiResponse
import com.robokassa.library.params.PaymentParams
import com.robokassa.library.syncServerTimeoutDefault
import com.robokassa.library.urlHoldingCancel
import com.robokassa.library.urlHoldingConfirm
import com.robokassa.library.urlSimpleSync
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

internal class ApiClient {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(syncServerTimeoutDefault, TimeUnit.MILLISECONDS)
        .readTimeout(syncServerTimeoutDefault, TimeUnit.MILLISECONDS)
        .also {
            if (Logger.logEnabled) {
                val interceptor = HttpLoggingInterceptor()
                interceptor.level = HttpLoggingInterceptor.Level.BODY
                it.addInterceptor(interceptor)
            }
        }.build()

    internal fun call(
        params: PaymentParams,
        apiMethod: ApiMethod,
        onSuccess: (RoboApiResponse) -> Unit,
        onFailure: (RoboApiException) -> Unit
    ) {

        try {
            val httpRequest = params.httpRequest(apiMethod)
            okHttpClient.newCall(httpRequest).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onFailure(
                        RoboApiException(
                            "Unable to performRequest request $apiMethod",
                            CheckPayState(
                                CheckRequestCode.TIMEOUT_ERROR,
                                CheckPayStateCode.STOPPED
                            ),
                            e
                        )
                    )
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        val responseCode = response.code
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            val result = if (apiMethod == ApiMethod.CHECK) {
                                CheckPayState.parse(responseBody)
                            } else {
                                PayActionState.parse(responseBody)
                            }
                            onSuccess(result)
                        } else {
                            onFailure(
                                RoboApiException(
                                    "Unsuccessful response $apiMethod, code $responseCode",
                                    CheckPayState(
                                        CheckRequestCode.SERVER_ERROR,
                                        CheckPayStateCode.STOPPED
                                    )
                                )
                            )
                        }
                    }
                }
            })
        } catch (e: Exception) {
            onFailure(
                RoboApiException(
                    "Unable to performRequest request $apiMethod",
                    e
                )
            )
        }
    }

    internal fun performRequestAsync(
        params: PaymentParams,
        apiMethod: ApiMethod
    ): Deferred<Result<RoboApiResponse>> {
        val deferred: CompletableDeferred<Result<RoboApiResponse>> = CompletableDeferred()
        call(
            params,
            apiMethod,
            onSuccess = {
                deferred.complete(Result.success(it))
            },
            onFailure = {
                deferred.complete(Result.failure(it))
            })
        return deferred
    }

    internal suspend fun performSuspendRequest(
        params: PaymentParams,
        apiMethod: ApiMethod
    ): Result<RoboApiResponse> {
        return performRequestAsync(params, apiMethod).run {
            start()
            await()
        }
    }

    private fun PaymentParams.httpRequest(apiMethod: ApiMethod) = Request.Builder().also { builder ->
        when (apiMethod) {
            ApiMethod.CHECK -> {
                builder.url("$urlSimpleSync?${this.checkPostParams()}")
                builder.get()
            }
            ApiMethod.HOLD_CONFIRM -> {
                builder.url(urlHoldingConfirm)
                val body = this.confirmHoldPostParams()
                builder.post(body.toRequestBody(FORM_URL_ENCODED.toMediaType()))
            }
            ApiMethod.HOLD_CANCEL -> {
                builder.url(urlHoldingCancel)
                val body = this.cancelHoldPostParams()
                builder.post(body.toRequestBody(FORM_URL_ENCODED.toMediaType()))
            }
        }
    }.build()

}