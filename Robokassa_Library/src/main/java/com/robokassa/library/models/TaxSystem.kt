package com.robokassa.library.models

import com.google.gson.annotations.SerializedName

/**
 * Системан налогообложения.
 */
enum class TaxSystem {
    /** Общая СН. */
    @SerializedName("osn")
    OSN,
    /** Упрощенная СН (доходы). */
    @SerializedName("usn_income")
    USN_INCOME,
    /** Упрощенная СН (доходы минус расходы). */
    @SerializedName("usn_income_outcome")
    USN_INCOME_OUTCOME,
    /** Единый сельскохозяйственный налог. */
    @SerializedName("esn")
    ESN,
    /** Патентная СН. */
    @SerializedName("patent")
    PATENT
}