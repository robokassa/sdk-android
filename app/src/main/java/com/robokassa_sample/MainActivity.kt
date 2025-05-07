package com.robokassa_sample

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.robokassa.library.helper.Logger
import com.robokassa.library.helper.toParams
import com.robokassa.library.models.CheckPayStateCode
import com.robokassa.library.models.CheckRequestCode
import com.robokassa.library.models.Culture
import com.robokassa.library.models.PayActionState
import com.robokassa.library.models.PayRecurrentState
import com.robokassa.library.models.PaymentMethod
import com.robokassa.library.models.Receipt
import com.robokassa.library.models.ReceiptItem
import com.robokassa.library.models.Tax
import com.robokassa.library.params.PaymentParams
import com.robokassa.library.pay.PaymentAction
import com.robokassa.library.pay.RobokassaPayLauncher
import com.robokassa.library.view.RobokassaViewModel
import com.robokassa_sample.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    companion object {
        const val MERCHANT = BuildConfig.MERCHANT
        const val PWD_1 = BuildConfig.PWD_1
        const val PWD_2 = BuildConfig.PWD_2
        const val PWD_TEST_1 = BuildConfig.PWD_TEST_1
        const val PWD_TEST_2 = BuildConfig.PWD_TEST_2
        const val REDIRECT_URL = BuildConfig.REDIRECT_URL
    }

    private val payProcess = registerForActivityResult(RobokassaPayLauncher.Contract) { result ->
        when (result) {
            is RobokassaPayLauncher.Success -> {
                if (result.stateCode == CheckPayStateCode.HOLD_SUCCESS) {
                    showHoldingMessage()
                } else {
                    if (params?.order?.isRecurrent == true) {
                        showRecurrentMessage(result.invoiceId)
                    } else if (needCheckSaving) {
                        showSavingMessage(result.invoiceId, result.opKey)
                    } else {
                        showAnswerMessage("Статус запроса: " + decryptRequest(result.resultCode) + ", Статус ответа: " + decryptResponse(result.stateCode))
                    }
                }
            }
            is RobokassaPayLauncher.Canceled -> {
                params = null
            }
            is RobokassaPayLauncher.Error -> {
                showAnswerMessage(
                    "Статус запроса: " + decryptRequest(result.resultCode) + ", Статус ответа: " + decryptResponse(result.stateCode) + ", Desc: " + result.desc + ", Error: " + result.error
                )
                params = null
            }
        }
    }

    private lateinit var binding: ActivityMainBinding

    private var params: PaymentParams? = null

    private var needCheckSaving = false
    private var testMode = false

    private val sampleReceipt = Receipt(
        items = listOf(
            ReceiptItem(
                name = "Ботинки детские",
                sum = 0.1,
                quantity = 1,
                paymentMethod = PaymentMethod.FULL_PAYMENT,
                tax = Tax.NONE
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Logger.logEnabled = true
        binding.orderNumber.setRawInputType(Configuration.KEYBOARDHIDDEN_YES)
        binding.simplePayButton.setOnClickListener {
            needCheckSaving = false
            simplePayClick()
        }
        binding.holdingPayButton.setOnClickListener {
            needCheckSaving = false
            holdingPayClick()
        }
        binding.recurrentPayButton.setOnClickListener {
            needCheckSaving = false
            recurrentPayClick()
        }
        binding.savingPayButton.setOnClickListener {
            needCheckSaving = true
            savingPayClick()
        }
        binding.logButton.setOnClickListener {
            getLogUri()
        }
        checkIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkIntent(intent)
    }

    private fun simplePayClick() {
        if (binding.orderNumber.text.isNullOrEmpty()) {
            showAnswerMessage(getString(R.string.app_order_hint))
        } else if (binding.orderNumber.text.toString().toIntOrNull() == null) {
            showAnswerMessage(getString(R.string.app_number_order_incorrect))
        } else {
            params = PaymentParams().setParams {
                orderParams {
                    invoiceId = binding.orderNumber.text.toString().toInt()
                    description = "Test Simple Pay"
                    orderSum = binding.orderSum.text.toString().toDoubleOrNull() ?: 0.1
                    receipt = sampleReceipt
                    expirationDate = Date(Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, 1)
                    }.timeInMillis)
                }
                customerParams {
                    culture = Culture.RU
                    email = "john@doe.com"
                }
                viewParams {
                    toolbarText = "Простая оплата"
                    toolbarTextColor = "#ff0000"
                }
            }.also {
                it.setCredentials(MERCHANT, getPwd1(), getPwd2(), REDIRECT_URL)
            }
            params?.let {
                payProcess.launch(RobokassaPayLauncher.StartPay(it, testMode = testMode))
            }
        }
    }

    private fun holdingPayClick() {
        if (binding.orderNumber.text.isNullOrEmpty()) {
            showAnswerMessage(getString(R.string.app_order_hint))
        } else if (binding.orderNumber.text.toString().toIntOrNull() == null) {
            showAnswerMessage(getString(R.string.app_number_order_incorrect))
        } else {
            params = PaymentParams().setParams {
                orderParams {
                    invoiceId = binding.orderNumber.text.toString().toInt()
                    description = "Test Simple Pay"
                    orderSum = 0.1
                    receipt = sampleReceipt
                    isHold = true
                }
                customerParams {
                    culture = Culture.RU
                    email = "john@doe.com"
                }
                viewParams {
                    toolbarText = "Холдирование"
                    toolbarTextColor = "#aaaaaa"
                }
            }.also {
                it.setCredentials(MERCHANT, getPwd1(), getPwd2(), REDIRECT_URL)
            }
            params?.let {
                payProcess.launch(RobokassaPayLauncher.StartPay(it, testMode = testMode))
            }
        }
    }

    private fun recurrentPayClick() {
        if (binding.orderNumber.text.isNullOrEmpty()) {
            showAnswerMessage(getString(R.string.app_order_hint))
        } else if (binding.orderNumber.text.toString().toIntOrNull() == null) {
            showAnswerMessage(getString(R.string.app_number_order_incorrect))
        } else {
            params = PaymentParams().setParams {
                orderParams {
                    invoiceId = binding.orderNumber.text.toString().toInt()
                    description = "Test Simple Pay"
                    orderSum = 0.1
                    receipt = sampleReceipt
                    isRecurrent = true
                }
                customerParams {
                    culture = Culture.RU
                    email = "john@doe.com"
                }
                viewParams {
                    toolbarText = "Рекуррентный платеж"
                    toolbarTextColor = "#cccccc"
                }
            }.also {
                it.setCredentials(MERCHANT, getPwd1(), getPwd2(), REDIRECT_URL)
            }
            params?.let {
                payProcess.launch(RobokassaPayLauncher.StartPay(it, testMode = testMode))
            }
        }
    }

    private fun savingPayClick() {
        if (binding.orderNumber.text.isNullOrEmpty()) {
            showAnswerMessage(getString(R.string.app_order_hint))
        } else if (binding.orderNumber.text.toString().toIntOrNull() == null) {
            showAnswerMessage(getString(R.string.app_number_order_incorrect))
        } else {
            params = PaymentParams().setParams {
                orderParams {
                    invoiceId = binding.orderNumber.text.toString().toInt()
                    description = "Test Simple Pay"
                    orderSum = 0.1
                    receipt = sampleReceipt
                    expirationDate = Date(Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, 1)
                    }.timeInMillis)
                }
                customerParams {
                    culture = Culture.RU
                    email = "john@doe.com"
                }
                viewParams {
                    toolbarText = "Простая оплата"
                    toolbarTextColor = "#ff0000"
                }
            }.also {
                it.setCredentials(MERCHANT, getPwd1(), getPwd2(), REDIRECT_URL)
            }
            params?.let {
                payProcess.launch(RobokassaPayLauncher.StartPay(it, testMode = testMode))
            }
        }
    }


    private fun showAnswerMessage(description: String) {
        AlertDialog.Builder(this)
            .setPositiveButton(android.R.string.ok) { _, _ ->

            }
            .setMessage(description)
            .show()
    }

    private fun decryptRequest(code: CheckRequestCode?): String {
        return when (code) {
            CheckRequestCode.CHECKING -> "Идет обработка запроса"
            CheckRequestCode.SUCCESS -> "Запрос обработан успешно"
            CheckRequestCode.SIGNATURE_ERROR -> "Неверная цифровая подпись запроса"
            CheckRequestCode.SHOP_ERROR -> "Информация о магазине с таким MerchantLogin не найдена или магазин не активирован"
            CheckRequestCode.INVOICE_ZERO_ERROR -> "Информация об операции с таким InvoiceID не найдена"
            CheckRequestCode.INVOICE_DOUBLE_ERROR -> "Найдено две операции с таким InvoiceID"
            CheckRequestCode.TIMEOUT_ERROR -> "Операция прервана по таймауту"
            CheckRequestCode.SERVER_ERROR -> "Внутренняя ошибка сервиса"
            else -> ""
        }
    }

    private fun decryptResponse(code: CheckPayStateCode?): String {
        return when (code) {
            CheckPayStateCode.NOT_INITED -> "Операция не инициализирована"
            CheckPayStateCode.INITED_NOT_PAYED -> "Операция только инициализирована, деньги от покупателя не получены"
            CheckPayStateCode.CANCELLED_NOT_PAYED -> "Операция отменена, деньги от покупателя не были получены"
            CheckPayStateCode.HOLD_SUCCESS -> "Операция находится в статусе HOLD"
            CheckPayStateCode.PAYED_NOT_TRANSFERRED -> "Деньги от покупателя получены, производится зачисление денег на счет магазина"
            CheckPayStateCode.PAYMENT_PAYBACK -> "Деньги после получения были возвращены покупателю"
            CheckPayStateCode.PAYMENT_STOPPED -> "Исполнение операции приостановлено"
            CheckPayStateCode.PAYMENT_SUCCESS -> "Платёж проведён успешно"
            else -> ""
        }
    }

    private fun showHoldingMessage() {
        AlertDialog.Builder(this)
            .setTitle(R.string.app_hold_title)
            .setNegativeButton(R.string.app_hold_confirm) { _, _ ->
                params?.let {
                    val pa = PaymentAction.init()
                    pa.confirmHold(it)
                    lifecycleScope.launch {
                        pa.state.collect { ps ->
                            if (ps is PayActionState) {
                                if (ps.success) {
                                    showAnswerMessage(getString(R.string.app_hold_confirm_success))
                                } else {
                                    showAnswerMessage(getString(R.string.app_hold_confirm_fail))
                                }
                            }
                        }
                    }
                }
            }
            .setPositiveButton(R.string.app_hold_cancel) { _, _ ->
                params?.let {
                    val pa = PaymentAction.init()
                    pa.cancelHold(it)
                    lifecycleScope.launch {
                        pa.state.collect { ps ->
                            if (ps is PayActionState) {
                                if (ps.success) {
                                    showAnswerMessage(getString(R.string.app_hold_cancel_success))
                                } else {
                                    showAnswerMessage(getString(R.string.app_hold_cancel_fail))
                                }
                            }
                        }
                    }
                }
            }
            .show()
    }

    private fun showRecurrentMessage(invoice: Int?) {
        invoice ?: return
        AlertDialog.Builder(this)
            .setTitle(R.string.app_button_recurrent_pay)
            .setPositiveButton(R.string.app_recurrent_confirm) { _, _ ->
                val recurrentParams = PaymentParams().setParams {
                    orderParams {
                        invoiceId = invoice + 1
                        previousInvoiceId = invoice
                        orderSum = 0.1
                        receipt = sampleReceipt
                    }
                }.also {
                    it.setCredentials(MERCHANT, getPwd1(), getPwd2(), REDIRECT_URL)
                }
                val pa = PaymentAction.init()
                pa.payRecurrent(recurrentParams)
                lifecycleScope.launch {
                    pa.state.collect { ps ->
                        if (ps is PayRecurrentState) {
                            if (ps.success) {
                                showAnswerMessage(getString(R.string.app_recurrent_confirm_success))
                            } else {
                                showAnswerMessage(getString(R.string.app_recurrent_confirm_fail))
                            }
                        }
                    }
                }
            }
            .show()
    }

    private fun showSavingMessage(invoice: Int?, opKey: String?) {
        invoice ?: return
        opKey ?: return
        AlertDialog.Builder(this)
            .setTitle(R.string.app_button_saving_pay)
            .setPositiveButton(R.string.app_recurrent_confirm) { _, _ ->
                needCheckSaving = false
                val recurrentParams = PaymentParams().setParams {
                    orderParams {
                        invoiceId = invoice + 1
                        description = "Test Saving Card Pay"
                        orderSum = 0.1
                        token = opKey
                        receipt = sampleReceipt
                    }
                    customerParams {
                        culture = Culture.RU
                        email = "john@doe.com"
                    }
                    viewParams {
                        toolbarText = "Оплата сохраненной картой"
                        toolbarTextColor = "#ff0000"
                    }
                }.also {
                    it.setCredentials(MERCHANT, getPwd1(), getPwd2(), REDIRECT_URL)
                }
                payProcess.launch(RobokassaPayLauncher.StartPay(recurrentParams, testMode = testMode))
            }
            .show()
    }

    private fun getPwd1() = if (testMode) PWD_TEST_1 else PWD_1

    private fun getPwd2() = if (testMode) PWD_TEST_2 else PWD_2

    private fun checkIntent(i : Intent?) {
        val data = i?.data
        if (data?.path?.endsWith("success.html") == true) {
            // Here you can handle success case
            showAnswerMessage(
                "Платёж проведён успешно"
            )
        } else if (data?.path?.endsWith("fail.html") == true) {
            // Here you can handle success case
            showAnswerMessage(
                "Платеж завершился с ошибкой"
            )
        } else if (data?.scheme == "robokassa") {
            val prefs = getSharedPreferences("robokassa.pay.prefs", Context.MODE_PRIVATE)
            val paramStr = prefs.getString("pay", "")
            try {
                paramStr.toParams()?.let {
                    payProcess.launch(RobokassaPayLauncher.StartPay(it, testMode = testMode, onlyCheck = true))
                } ?: run {
                    showAnswerMessage(
                        "Нет сохраненных платежных данных"
                    )
                }
            } catch (e: Exception) {
                showAnswerMessage(
                    "Нет сохраненных платежных данных"
                )
            }

        }
    }

    private fun getLogUri() {
        val formatter = SimpleDateFormat("dd.MM.yyyy_HH.mm.ss", Locale.getDefault())
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val logFile = withContext(Dispatchers.IO) {
                    val logFile = File(
                        filesDir,
                        "Log_${formatter.format(Date())}.txt"
                    )
                    logFile.delete()
                    if (!logFile.exists()) {
                        logFile.createNewFile()
                    }
                    val c = Calendar.getInstance()
                    c.set(Calendar.HOUR_OF_DAY, 0)
                    c.set(Calendar.MINUTE, 0)
                    c.set(Calendar.SECOND, 0)
                    val abc = RobokassaViewModel.logs
                    logFile.writeText(abc.joinToString(separator = "\n"))
                    logFile
                }
                val emailIntent = Intent(Intent.ACTION_SEND)
                emailIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("john@doe.com"))
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support")
                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                emailIntent.putExtra(
                    Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                        this@MainActivity,
                        "${packageName}.provider", logFile
                    )
                )
                emailIntent.type = "text/plain"
                startActivity(Intent.createChooser(emailIntent, "Send mail using..."))
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, e.message ?: e.toString(), Toast.LENGTH_LONG).show()
                }
            }

        }

    }

}