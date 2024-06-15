package com.robokassa.library.models

import com.google.gson.annotations.SerializedName

/**
 * Объект налоговой ставки.
 */
enum class Tax {
    /** Без НДС. */
    @SerializedName("none")
    NONE,
    /** НДС по ставке 0%. */
    @SerializedName("vat0")
    VAT_0,
    /** НДС чека по ставке 10%. */
    @SerializedName("vat10")
    VAT_10,
    /** НДС чека по расчетной ставке 10/110. */
    @SerializedName("vat110")
    VAT_110,
    /** НДС чека по ставке 20%. */
    @SerializedName("vat20")
    VAT_20,
    /** НДС чека по расчетной ставке 20/120. */
    @SerializedName("vat120")
    VAT_120
}