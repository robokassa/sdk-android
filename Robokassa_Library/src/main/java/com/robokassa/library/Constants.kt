package com.robokassa.library

internal const val LOG_TAG = "okh2"

internal const val EXTRA_ERROR = "com.robokassa.PAY_ERROR"
internal const val EXTRA_PARAMS = "com.robokassa.PAYMENT_PARAMS"
internal const val EXTRA_INVOICE_ID = "com.robokassa.PAYMENT_INVOICE_ID"
internal const val EXTRA_TEST_PARAMETERS = "com.robokassa.TEST_PARAMETERS"
internal const val EXTRA_CODE_RESULT = "com.robokassa.PAYMENT_CODE_RESULT"
internal const val EXTRA_CODE_STATE = "com.robokassa.PAYMENT_CODE_STATE"
internal const val EXTRA_ERROR_DESC = "com.robokassa.PAYMENT_ERROR_DESC"
internal const val EXTRA_OP_KEY = "com.robokassa.PAYMENT_OP_KEY"

internal const val FORM_URL_ENCODED = "application/x-www-form-urlencoded"

internal const val syncServerTimeDefault : Long = 2000
internal const val syncServerTimeoutDefault : Long = 40000

internal const val urlMain = "https://auth.robokassa.ru/Merchant/Index.aspx"
internal const val urlSaving = "https://auth.robokassa.ru/Merchant/Payment/CoFPayment"
internal const val urlSimpleSync = "https://auth.robokassa.ru/Merchant/WebService/Service.asmx/OpStateExt"
internal const val urlHoldingConfirm = "https://auth.robokassa.ru/Merchant/Payment/Confirm"
internal const val urlHoldingCancel = "https://auth.robokassa.ru/Merchant/Payment/Cancel"
internal const val urlRecurring = "https://auth.robokassa.ru/Merchant/Recurring"
