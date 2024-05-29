package com.robokassa.library.Assistant

import java.math.BigInteger
import java.security.MessageDigest

public object RobokassaMD5 {
    fun md5Hash(str: String): String {
        val md = MessageDigest.getInstance("MD5")
        val bigInt = BigInteger(1, md.digest(str.toByteArray(Charsets.UTF_8)))
        return String.format("%032x", bigInt)
    }
}