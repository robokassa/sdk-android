package com.robokassa.library.SimplePay

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebSettings.LOAD_NORMAL
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.EncodingUtils
import com.robokassa.library.Assistant.PaymentParameters
import com.robokassa.library.Assistant.RobokassaAnswer
import com.robokassa.library.Assistant.RobokassaPayMetod
import com.robokassa.library.Assistant.RobokassaResultCallBack
import com.robokassa.library.Assistant.RobokassaResultTimer
import com.robokassa.library.Assistant.RobokassaResultTimerCallback
import com.robokassa.library.Assistant.RobokassaResultTimerCode
import com.robokassa.library.Assistant.RobokassaResultTimerState
import com.robokassa.library.EXTRA_METOD_CODE
import com.robokassa.library.EXTRA_METOD_CODE_STATE
import com.robokassa.library.EXTRA_METOD_PARAMETERS
import com.robokassa.library.EXTRA_METOD_SYNC_SERVER_TIME
import com.robokassa.library.EXTRA_METOD_SYNC_SERVER_TIMEOUT
import com.robokassa.library.EXTRA_PAYMENT_PARAMETERS
import com.robokassa.library.EXTRA_TEST_PARAMETERS
import com.robokassa.library.R
import com.robokassa.library.SimplePay.RobokassaSimplePay.paramsSimplePay
import com.robokassa.library.databinding.ActivityRobokassaBinding
import com.robokassa.library.syncServerTimeDefault
import com.robokassa.library.syncServerTimeoutDefault
import com.robokassa.library.urlMain
import com.robokassa.library.urlSimpleSync


@Suppress("DEPRECATION")
class RobokassaActivity : AppCompatActivity() {

    lateinit private var binding: ActivityRobokassaBinding
    lateinit var paymentParameters: PaymentParameters
    lateinit var robokassaPayMetod: RobokassaPayMetod
    private var testMode = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityRobokassaBinding.inflate(layoutInflater)
        binding.toolbar.setNavigationIcon(R.drawable.app_back_arrow)
        setSupportActionBar(binding.toolbar)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            paymentParameters = intent.getParcelableExtra(EXTRA_PAYMENT_PARAMETERS, PaymentParameters::class.java)!!
        }else{
            paymentParameters = intent?.getParcelableExtra(EXTRA_PAYMENT_PARAMETERS)!!
        }

        robokassaPayMetod = intent.getSerializableExtra(EXTRA_METOD_PARAMETERS) as RobokassaPayMetod

        testMode = intent.getBooleanExtra( EXTRA_TEST_PARAMETERS, false);
        val syncTime = intent.getLongExtra( EXTRA_METOD_SYNC_SERVER_TIME, syncServerTimeDefault)
        val syncTimeout = intent.getLongExtra(EXTRA_METOD_SYNC_SERVER_TIMEOUT, syncServerTimeoutDefault)


        initWebView()
        initStatusTimer(syncTime, syncTimeout)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    private fun configureWebSettings() {

        CookieManager.getInstance().setAcceptCookie(true)

        val webSettings: WebSettings = binding.webView.settings

        webSettings.apply {
            cacheMode = LOAD_NORMAL
            domStorageEnabled = true
            javaScriptEnabled = true
            allowFileAccess = true
            allowFileAccessFromFileURLs = true
            allowContentAccess  = true
            allowFileAccessFromFileURLs = true
        }
    }

    private fun initWebView() {
        configureWebSettings()

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                Log.v("TEST","WORK!!!!!!")
                if (request?.isRedirect == true) {
                    return true
                }
                return true
            }
        }

        val urlParams = paramsSimplePay(paymentParameters, testMode, robokassaPayMetod)
        binding.webView.webViewClient = WebViewClient()
        binding.webView.apply {
            postUrl(urlMain, EncodingUtils.getBytes(urlParams.requestParams, "BASE64"))
        }

    }

    private fun initStatusTimer(syncTime: Long, syncTimeout: Long) {
        RobokassaResultTimer().initTimer(paymentParameters, urlSimpleSync,syncTime,syncTimeout, object: RobokassaResultTimerCallback{
            override fun result(robokassaAnswer: RobokassaAnswer) {

                val data = Intent()
                data.putExtra(EXTRA_METOD_CODE, robokassaAnswer.code.value)
                data.putExtra(EXTRA_METOD_CODE_STATE, robokassaAnswer.stateCode.value)
                setResult(RESULT_OK, data)
                if(robokassaAnswer.stateCode == RobokassaResultTimerState.CODE_10 || robokassaAnswer.stateCode == RobokassaResultTimerState.CODE_20 || robokassaAnswer.stateCode == RobokassaResultTimerState.CODE_60 || robokassaAnswer.stateCode == RobokassaResultTimerState.CODE_80 || robokassaAnswer.code == RobokassaResultTimerCode.TIMEOUT)
                     finish()
            }
        })
    }




}