package com.robokassa.library.helper

import com.google.gson.Gson
import com.robokassa.library.helper.RobokassaDateHelper.toIsoString
import com.robokassa.library.params.PaymentParams
import java.math.BigInteger
import java.net.URLEncoder
import java.security.MessageDigest

fun PaymentParams.checkPostParams(): String = run {

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

    result += "&SignatureValue=$signatureValue"

    Logger.i("Post params: $result")

    result
}

fun PaymentParams.payPostParams(isTest: Boolean): String = run {

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

    this.customer.culture?.let {
        result += "&Culture=${it.iso}"
    }

    this.customer.email?.takeIf { it.isNotEmpty() }?.let {
        result += "&Email=$it"
    }

    this.customer.ip?.takeIf { it.isNotEmpty() }?.let {
        result += "&UserIp=$it"
    }

    if (isTest) {
        result += "&IsTest=1"
    }

    signature += ":" + this.password1

    val signatureValue = md5Hash(signature)

    Logger.i("Signature src: $signature")

    result += "&SignatureValue=$signatureValue"

    Logger.i("Post params: $result")

    result
}

fun PaymentParams.confirmHoldPostParams(): String = run {

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
        signature += ":$jsonEncoded"
    }

    signature += ":" + this.password1

    val signatureValue = md5Hash(signature)

    Logger.i("Signature src: $signature")

    result += "&SignatureValue=$signatureValue"

    Logger.i("Post params: $result")

    result
}

fun PaymentParams.cancelHoldPostParams(): String = run {

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

fun md5Hash(str: String): String {
    val md = MessageDigest.getInstance("MD5")
    val n =  BigInteger(1, md.digest(str.toByteArray(Charsets.UTF_8))).toString(16).padStart(32, '0')
    Logger.i("MD hash: $n")
    return n
}