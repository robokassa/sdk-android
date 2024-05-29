package com.robokassa.library.Assistant

import java.util.Locale

object RobokassaEncodingHelper {
    fun utf8ToUnicode(inStr: String): String {
        val myBuffer = inStr.toCharArray()

        val sb = StringBuffer()
        for (i in 0 until inStr.length) {
            val ub: Character.UnicodeBlock? = Character.UnicodeBlock.of(myBuffer[i])
            if (ub === Character.UnicodeBlock.BASIC_LATIN) {
                sb.append(myBuffer[i])
            } else if (ub === Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
                val j = myBuffer[i].code - 65248
                sb.append(j.toChar())
            } else {
                val s = myBuffer[i].code.toShort()
                val hexS = Integer.toHexString(s.toInt())
                val unicode = "\\u$hexS"
                sb.append(unicode.lowercase(Locale.getDefault()))
            }
        }
        return sb.toString()
    }
}