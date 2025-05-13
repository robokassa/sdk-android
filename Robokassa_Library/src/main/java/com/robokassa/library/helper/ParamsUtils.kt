package com.robokassa.library.helper

import com.google.gson.Gson
import com.robokassa.library.params.PaymentParams
import java.math.BigInteger
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val ISO_8601_24H_FULL_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

fun String?.toParams(): PaymentParams? {
    this ?: return null
    return Gson().fromJson(this, PaymentParams::class.java)
}

internal fun PaymentParams.checkPostParams(): String = run {

    var result = ""
    var signature = ""

    result += "MerchantLogin=" + this.merchantLogin
    signature += this.merchantLogin

    this.order.invoiceId.takeIf { it > 0 }?.let {
        val id = it.toString()
        result += "&invoiceID=$id"
        signature += ":$id"
    } ?: run {
        signature += ":"
    }

    signature += ":" + this.password2

    val signatureValue = md5Hash(signature)

    Logger.i("Signature src: $signature")

    result += "&Signature=$signatureValue"

    Logger.i("Post params: $result")

    result
}

internal fun PaymentParams.payPostParams(isTest: Boolean): String = run {

    var result = ""
    var signature = ""

    result += "MerchantLogin=" + this.merchantLogin
    signature += this.merchantLogin

    this.order.description?.takeIf { it.isNotEmpty() }?.let {
        result += "&Description=$it"
    }

    this.order.orderSum.takeIf { it > 0.0 }?.let {
        val outSum = it.toString()
        result += "&OutSum=$outSum"
        signature += ":$outSum"
    }

    this.order.invoiceId.takeIf { it > 0 }?.let {
        val id = it.toString()
        result += "&invoiceID=$id"
        signature += ":$id"
    } ?: run {
        signature += ":"
    }

    this.order.receipt?.let {
        val gson = Gson()
        val json = gson.toJson(it)
        val jsonEncoded = URLEncoder.encode(json, "utf-8")
        result += "&Receipt=$jsonEncoded"
        signature += ":$json"
    }

    if (this.order.isHold) {
        result += "&StepByStep=true"
        signature += ":" + "true"
    }

    if (this.order.isRecurrent) {
        result += "&Recurring=true"
    }

    this.order.expirationDate?.let {
        result += "&ExpirationDate=" + it.toIsoString()
    }

    this.order.incCurrLabel?.takeIf { it.isNotEmpty() }?.let {
        result += "&IncCurrLabel=$it"
    }

    this.order.token?.takeIf { it.isNotEmpty() }?.let {
        result += "&Token=$it"
        signature += ":$it"
    }

    this.customer.culture?.let {
        result += "&Culture=${it.iso}"
    }

    this.customer.email?.takeIf { it.isNotEmpty() }?.let {
        result += "&Email=$it"
    }

    if (isTest) {
        result += "&IsTest=1"
    }
    result += "&shp_label=sdk_android"
    signature += ":" + this.password1 + ":shp_label=sdk_android"

    val signatureValue = md5Hash(signature)

    Logger.i("Signature src: $signature")

    result += "&SignatureValue=$signatureValue"

    Logger.i("Post params: $result")
    Logger.i("Post token: ${this.order.token}")

    result
}

internal fun PaymentParams.confirmHoldPostParams(): String = run {

    var result = ""
    var signature = ""

    result += "MerchantLogin=" + this.merchantLogin
    signature += this.merchantLogin

    this.order.orderSum.takeIf { it > 0.0 }?.let {
        val outSum = it.toString()
        result += "&OutSum=$outSum"
        signature += ":$outSum"
    }

    this.order.invoiceId.takeIf { it > 0 }?.let {
        val id = it.toString()
        result += "&invoiceID=$id"
        signature += ":$id"
    } ?: run {
        signature += ":"
    }

    this.order.receipt?.let {
        val gson = Gson()
        val json = gson.toJson(it)
        val jsonEncoded = URLEncoder.encode(json, "utf-8")
        result += "&Receipt=$jsonEncoded"
        signature += ":$json"
    }

    signature += ":" + this.password1

    val signatureValue = md5Hash(signature)

    Logger.i("Signature src: $signature")

    result += "&SignatureValue=$signatureValue"

    Logger.i("Post params: $result")

    result
}

internal fun PaymentParams.cancelHoldPostParams(): String = run {

    var result = ""
    var signature = ""

    result += "MerchantLogin=" + this.merchantLogin
    signature += this.merchantLogin

    this.order.orderSum.takeIf { it > 0.0 }?.let {
        val outSum = it.toString()
        result += "&OutSum=$outSum"
    }

    this.order.invoiceId.takeIf { it > 0 }?.let {
        val id = it.toString()
        result += "&invoiceID=$id"
        signature += "::$id"
    } ?: run {
        signature += "::"
    }

    signature += ":" + this.password1

    val signatureValue = md5Hash(signature)

    Logger.i("Signature src: $signature")

    result += "&SignatureValue=$signatureValue"

    Logger.i("Post params: $result")

    result
}

internal fun PaymentParams.recurrentPostParams(): String = run {

    var result = ""
    var signature = ""

    result += "MerchantLogin=" + this.merchantLogin
    signature += this.merchantLogin

    this.order.orderSum.takeIf { it > 0.0 }?.let {
        val outSum = it.toString()
        result += "&OutSum=$outSum"
        signature += ":$outSum"
    }

    this.order.invoiceId.takeIf { it > 0 }?.let {
        val id = it.toString()
        result += "&invoiceID=$id"
        signature += ":$id"
    } ?: run {
        signature += ":"
    }

    this.order.previousInvoiceId.takeIf { it > 0 }?.let {
        val id = it.toString()
        result += "&PreviousInvoiceID=$id"
    }

    this.order.receipt?.let {
        val gson = Gson()
        val json = gson.toJson(it)
        val jsonEncoded = URLEncoder.encode(json, "utf-8")
        result += "&Receipt=$jsonEncoded"
        signature += ":$json"
    }

    signature += ":" + this.password1

    val signatureValue = md5Hash(signature)

    Logger.i("Signature src: $signature")

    result += "&SignatureValue=$signatureValue"

    Logger.i("Post params: $result")

    result
}

private fun md5Hash(str: String): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(str.toByteArray(Charsets.UTF_8))).toString(16).padStart(32, '0')
}

private fun Date.toIsoString(): String {
    val dateFormat: DateFormat = SimpleDateFormat(ISO_8601_24H_FULL_FORMAT, Locale.getDefault())
    return dateFormat.format(this)
}