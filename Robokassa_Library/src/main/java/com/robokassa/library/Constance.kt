package com.robokassa.library

internal const val EXTRA_PAYMENT_PARAMETERS = "com.robokassa.PAYMENT_PARAMETERS"
internal const val EXTRA_CSC_PARAMETERS = "com.robokassa.CSC_PARAMETERS"
internal const val EXTRA_TEST_PARAMETERS = "com.robokassa.TEST_PARAMETERS"
internal const val EXTRA_METOD_PARAMETERS = "com.robokassa.METOD_PARAMETERS"
internal const val EXTRA_METOD_SYNC_SERVER_TIME = "com.robokassa.METOD_SINC_SERVER_TIME"
internal const val EXTRA_METOD_SYNC_SERVER_TIMEOUT = "com.robokassa.METOD_SYNC_SERVER_TIMEOUT"
internal const val EXTRA_METOD_INTERFACE = "com.robokassa.METOD_INTERFACE"
internal const val EXTRA_METOD_CODE = "com.robokassa.METOD_CODE"
internal const val EXTRA_METOD_CODE_STATE = "com.robokassa.METOD_CODE_STATE"



internal const val syncServerTimeDefault : Long = 5000
internal const val syncServerTimeoutDefault : Long = 300000


internal const val urlMain = "https://auth.robokassa.ru/Merchant/Index.aspx"
internal const val urlSimpleSync = "https://auth.robokassa.ru/Merchant/WebService/Service.asmx/OpStateExt"
internal const val urlHoldingConfirm = "https://auth.robokassa.ru/Merchant/Payment/Confirm"
internal const val urlHoldingCancel = "https://auth.robokassa.ru/Merchant/Payment/Cancel"



object Constance {

}