package com.robokassa.library.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Объект фискального чека.
 */
data class Receipt(
    /**
     * Система налогообложения. Необязательное поле, если у организации имеется только один тип налогообложения.
     * (Данный параметр обзятально задается в личном кабинете магазина).
     * */
    @SerializedName("sno")
    val sno: TaxSystem? = null,
    /** Массив данных о позициях чека. */
    @SerializedName("items")
    val items: List<ReceiptItem>
) : Serializable
