package com.robokassa_sample

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.robokassa.library.helper.Logger
import com.robokassa.library.models.CheckPayStateCode
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
import com.robokassa_sample.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity() {

    companion object {
        const val MERCHANT = ""
        const val PWD_1 = ""
        const val PWD_2 = ""
        const val PWD_TEST_1 = ""
        const val PWD_TEST_2 = ""
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
                        showAnswerMessage("Code: " + result.resultCode + ", StatusCode: " + result.stateCode)
                    }
                }
            }
            is RobokassaPayLauncher.Canceled -> {
                params = null
            }
            is RobokassaPayLauncher.Error -> {
                showAnswerMessage(
                    "Code: " + result.resultCode + ", StatusCode: " + result.stateCode + ", Desc: " + result.desc + ", Error: " + result.error
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
    }

    private fun simplePayClick() {
        if (binding.orderNumber.text.isNullOrEmpty()) {
            showAnswerMessage(getString(R.string.app_order_hint))
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
                    email = "p.kolosov@list.ru"
                }
                viewParams {
                    toolbarText = "Простая оплата"
                    toolbarTextColor = "#ff0000"
                }
            }.also {
                it.setCredentials(MERCHANT, getPwd1(), getPwd2(), REDIRECT_URL)
            }
            params?.let {
                payProcess.launch(RobokassaPayLauncher.StartPay(it, testMode))
            }
        }
    }

    private fun holdingPayClick() {
        if (binding.orderNumber.text.isNullOrEmpty()) {
            showAnswerMessage(getString(R.string.app_order_hint))
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
                    email = "p.kolosov@list.ru"
                }
                viewParams {
                    toolbarText = "Холдирование"
                    toolbarTextColor = "#aaaaaa"
                }
            }.also {
                it.setCredentials(MERCHANT, getPwd1(), getPwd2(), REDIRECT_URL)
            }
            params?.let {
                payProcess.launch(RobokassaPayLauncher.StartPay(it, testMode))
            }
        }
    }

    private fun recurrentPayClick() {
        if (binding.orderNumber.text.isNullOrEmpty()) {
            showAnswerMessage(getString(R.string.app_order_hint))
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
                    email = "p.kolosov@list.ru"
                }
                viewParams {
                    toolbarText = "Рекуррентный платеж"
                    toolbarTextColor = "#cccccc"
                }
            }.also {
                it.setCredentials(MERCHANT, getPwd1(), getPwd2(), REDIRECT_URL)
            }
            params?.let {
                payProcess.launch(RobokassaPayLauncher.StartPay(it, testMode))
            }
        }
    }

    private fun savingPayClick() {
        if (binding.orderNumber.text.isNullOrEmpty()) {
            showAnswerMessage(getString(R.string.app_order_hint))
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
                    email = "p.kolosov@list.ru"
                }
                viewParams {
                    toolbarText = "Простая оплата"
                    toolbarTextColor = "#ff0000"
                }
            }.also {
                it.setCredentials(MERCHANT, getPwd1(), getPwd2(), REDIRECT_URL)
            }
            params?.let {
                payProcess.launch(RobokassaPayLauncher.StartPay(it, testMode))
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
                        email = "p.kolosov@list.ru"
                    }
                    viewParams {
                        toolbarText = "Оплата сохраненной картой"
                        toolbarTextColor = "#ff0000"
                    }
                }.also {
                    it.setCredentials(MERCHANT, getPwd1(), getPwd2(), REDIRECT_URL)
                }
                payProcess.launch(RobokassaPayLauncher.StartPay(recurrentParams, testMode))
            }
            .show()
    }

    private fun getPwd1() = if (testMode) PWD_TEST_1 else PWD_1

    private fun getPwd2() = if (testMode) PWD_TEST_2 else PWD_2

}