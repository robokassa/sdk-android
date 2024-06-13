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
import com.robokassa.library.models.PaymentMethod
import com.robokassa.library.models.Receipt
import com.robokassa.library.models.ReceiptItem
import com.robokassa.library.models.Tax
import com.robokassa.library.params.PaymentParams
import com.robokassa.library.pay.PaymentAction
import com.robokassa.library.pay.RobokassaPayLauncher
import com.robokassa_sample.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        const val MERCHANT = "ipolh.com"
        const val PWD_1 = "X7SlyJ9I4z50JpaiKCjj"
        const val PWD_2 = "Y7t35UJPLS4IZAAan7SP"
        const val PWD_TEST_1 = "o1zCrG7EHdB6TYPkt0K5"
        const val PWD_TEST_2 = "zgxF4Vf1oAv4k3uR7rZT"
    }

    private val payProcess = registerForActivityResult(RobokassaPayLauncher.Contract) { result ->
        when (result) {
            is RobokassaPayLauncher.Success -> {
                if (result.stateCode == CheckPayStateCode.HOLD_SUCCESS) {
                    showHoldingMessage()
                } else {
                    if (params?.order?.isRecurrent == true) {
                        showRecurrentMessage(result.invoiceId)
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
            simplePayClick()
        }
        binding.holdingPayButton.setOnClickListener {
            holdingPayClick()
        }
        binding.recurrentPayButton.setOnClickListener {
            recurrentPayClick()
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
                it.setCredentials(MERCHANT, PWD_1, PWD_2)
            }
            params?.let {
                payProcess.launch(RobokassaPayLauncher.StartPay(it))
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
                it.setCredentials(MERCHANT, PWD_1, PWD_2)
            }
            params?.let {
                payProcess.launch(RobokassaPayLauncher.StartPay(it))
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
                it.setCredentials(MERCHANT, PWD_1, PWD_2)
            }
            params?.let {
                payProcess.launch(RobokassaPayLauncher.StartPay(it))
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

    private fun showRecurrentMessage(invoiceId: Int?) {
        invoiceId ?: return
        AlertDialog.Builder(this)
            .setTitle(R.string.app_hold_title)
            .setNegativeButton(R.string.app_hold_confirm) { _, _ ->
                params?.let {

                }
            }
            .setPositiveButton(R.string.app_hold_cancel) { _, _ ->
                params?.let {

                }
            }
            .show()
    }
}