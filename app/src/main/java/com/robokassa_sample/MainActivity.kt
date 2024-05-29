package com.robokassa_sample

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.robokassa.library.Assistant.Culture
import com.robokassa.library.Assistant.PaymentParameters
import com.robokassa.library.Assistant.RobokassaAnswer
import com.robokassa.library.Assistant.RobokassaResultCallBack
import com.robokassa.library.Robokassa
import com.robokassa.library.Robokassa.holdingCancel
import com.robokassa.library.Robokassa.holdingComplete
import com.robokassa.library.RobokassaReceipt.ReceiptPaymentMethod
import com.robokassa.library.RobokassaReceipt.ReceiptTax
import com.robokassa.library.RobokassaReceipt.RobokassaReceipt
import com.robokassa.library.RobokassaReceipt.RobokassaReceiptItem
import com.robokassa_sample.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.orderNumber.setRawInputType(Configuration.KEYBOARDHIDDEN_YES)
    }

    fun simpleMetodOnClick(view: View){
        if(binding.orderNumber.text.isEmpty()){
            showAnswerMessage("Введите номер заказа")
        }else {
            val paymentParameters = PaymentParameters(
                merchantLogin = "ipolh.com",
                password_1 = "X7SlyJ9I4z50JpaiKCjj",//"o1zCrG7EHdB6TYPkt0K5",
                password_2 = "Y7t35UJPLS4IZAAan7SP",// "zgxF4Vf1oAv4k3uR7rZT",
                culture = Culture.ru,
                invoceID = binding.orderNumber.text.toString().toBigDecimal(),
                description = "desc123",
                outSum = 1.0
            )
            Robokassa.createSimplePay(
                this,
                paymentParameters,
                result = object : RobokassaResultCallBack {
                    override fun result(robokassaResult: RobokassaAnswer) {
                        showAnswerMessage("Code " + robokassaResult.code + "StatusCode " + robokassaResult.stateCode)
                    }
                })
        }
    }


    fun holdingMetodOnClick(view: View){
        showHoldingMessage();
    }

    fun recurrentMetodOnClick(view: View){

    }

    private fun showAnswerMessage(description: String){
        AlertDialog.Builder(this)
            .setTitle("Внимание")
            .setPositiveButton("OК"){ dialog, which ->

            }
            .setMessage(description)
            .show()
    }

    private fun showHoldingMessage(){

        AlertDialog.Builder(this)
            .setTitle("Холдирование средств")
            .setNegativeButton("Подтверждение холдирования"){ dialog, which ->

                val item = RobokassaReceiptItem(
                    name = "Обувь",
                    sum = 1.43,
                    quantity = 1,
                    payment_method = ReceiptPaymentMethod.partial_payment,
                    tax =  ReceiptTax.none)

                val receipt = RobokassaReceipt(items = arrayOf(item))

                val paymentParameters = PaymentParameters(
                    merchantLogin = "ipolh.com",
                    password_1 = "X7SlyJ9I4z50JpaiKCjj",//"o1zCrG7EHdB6TYPkt0K5",
                    password_2 = "Y7t35UJPLS4IZAAan7SP",// "zgxF4Vf1oAv4k3uR7rZT",
                    culture = Culture.ru,
                    receipt = receipt,
                    invoceID = binding.orderNumber.text.toString().toBigDecimal(),
                    description = "Test123",
                    outSum = 1.43
                )

                holdingComplete(
                     this,
                    paymentParameters,
                    false,
                    null)

            }
            .setNeutralButton("Отмена холдирования"){ _, _ ->



                val paymentParameters = PaymentParameters(
                    merchantLogin = "ipolh.com",
                    password_1 = "X7SlyJ9I4z50JpaiKCjj",//"o1zCrG7EHdB6TYPkt0K5",
                    password_2 = "Y7t35UJPLS4IZAAan7SP",// "zgxF4Vf1oAv4k3uR7rZT",
                    culture = Culture.ru,
                    invoceID = binding.orderNumber.text.toString().toBigDecimal(),
                    description = "Test123",
                    outSum = 1.43
                )

                holdingCancel(
                    this,
                    paymentParameters,
                    false,
                    null)
            }

            .setPositiveButton("Холдирование"){ _, _ ->

                if(binding.orderNumber.text.isEmpty()){
                    showAnswerMessage("Введите номер заказа")
                }else {
                    val item = RobokassaReceiptItem(
                        name = "Обувь",
                        sum = 1.43,
                        quantity = 1,
                        payment_method = ReceiptPaymentMethod.partial_payment,
                        tax =  ReceiptTax.none)

                    val receipt = RobokassaReceipt(items = arrayOf(item))

                    val paymentParameters = PaymentParameters(
                        merchantLogin = "ipolh.com",
                        password_1 = "X7SlyJ9I4z50JpaiKCjj",//"o1zCrG7EHdB6TYPkt0K5",
                        password_2 = "Y7t35UJPLS4IZAAan7SP",// "zgxF4Vf1oAv4k3uR7rZT",
                        culture = Culture.ru,
                        receipt = receipt,
                        invoceID = binding.orderNumber.text.toString().toBigDecimal(),
                        description = "Test123",
                        outSum = 1.43
                    )

                    Robokassa.createHoldingPay(
                        this,
                        paymentParameters,
                        result = object : RobokassaResultCallBack {
                            override fun result(robokassaResult: RobokassaAnswer) {
                                showAnswerMessage("Code " + robokassaResult.code + "StatusCode " + robokassaResult.stateCode)
                            }
                        })
                }

            }
            .show()
    }
}