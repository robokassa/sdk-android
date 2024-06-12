package com.robokassa_sample

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.robokassa.library.helper.Logger
import com.robokassa.library.models.Culture
import com.robokassa.library.models.PaymentMethod
import com.robokassa.library.models.Receipt
import com.robokassa.library.models.ReceiptItem
import com.robokassa.library.models.Tax
import com.robokassa.library.params.PaymentParams
import com.robokassa.library.pay.RobokassaPayLauncher
import com.robokassa_sample.databinding.ActivityMainBinding

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
                showAnswerMessage("Code: " + result.resultCode + ", StatusCode: " + result.stateCode)
            }
            is RobokassaPayLauncher.Canceled -> {
                params = null
            }
            is RobokassaPayLauncher.Error -> {
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
                //paymentMethod = PaymentMethod.FULL_PAYMENT,
                //tax = Tax.NONE
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
                }
                viewParams {
                    toolbarText = "Простая оплата"
                    toolbarTextColor = "#ffffff"
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
        showHoldingMessage()
    }

    private fun recurrentPayClick() {

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

                }
            }
            .setNeutralButton(R.string.app_hold_cancel) { _, _ ->
                params?.let {

                }
            }
            .setPositiveButton(R.string.app_hold_start) { _, _ ->
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
                        }
                    }.also {
                        it.setCredentials(MERCHANT, PWD_1, PWD_2)
                    }
                    params?.let {
                        payProcess.launch(RobokassaPayLauncher.StartPay(it))
                    }
                }
            }
            .show()
    }
}