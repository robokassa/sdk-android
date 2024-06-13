package com.robokassa.library.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebSettings.LOAD_NORMAL
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.EncodingUtils
import com.robokassa.library.EXTRA_CODE_RESULT
import com.robokassa.library.EXTRA_CODE_STATE
import com.robokassa.library.EXTRA_ERROR
import com.robokassa.library.EXTRA_ERROR_DESC
import com.robokassa.library.EXTRA_INVOICE_ID
import com.robokassa.library.EXTRA_PARAMS
import com.robokassa.library.EXTRA_TEST_PARAMETERS
import com.robokassa.library.R
import com.robokassa.library.databinding.ActivityRobokassaBinding
import com.robokassa.library.helper.Logger
import com.robokassa.library.helper.payPostParams
import com.robokassa.library.models.CheckPayStateCode
import com.robokassa.library.models.CheckRequestCode
import com.robokassa.library.params.PaymentParams
import com.robokassa.library.urlMain
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class RobokassaActivity : AppCompatActivity() {

    companion object {
        fun intent(options: PaymentParams, context: Context): Intent {
            val intent = Intent(context, RobokassaActivity::class.java)
            intent.putExtra(EXTRA_PARAMS, options)
            return intent
        }
    }

    private lateinit var binding: ActivityRobokassaBinding
    private lateinit var paymentParams: PaymentParams
    private var testMode = false

    private val model: RobokassaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRobokassaBinding.inflate(layoutInflater)
        binding.toolbar.setNavigationIcon(R.drawable.app_back_arrow)
        setSupportActionBar(binding.toolbar)
        setContentView(binding.root)
        val pp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(EXTRA_PARAMS, PaymentParams::class.java)
        } else {
            intent?.getParcelableExtra(EXTRA_PARAMS)
        }
        if (pp == null) {
            finish()
        } else {
            paymentParams = pp
        }
        testMode = intent.getBooleanExtra(EXTRA_TEST_PARAMETERS, false)
        binding.toolbar.isVisible = paymentParams.view.hasToolbar
        if (paymentParams.view.hasToolbar) {
            supportActionBar?.title = ""
            binding.toolbarTitle.text = paymentParams.view.toolbarText.takeIf {
                it.isNullOrEmpty().not()
            } ?: getString(R.string.pay_title)
            try {
                binding.toolbar.setBackgroundColor(
                    Color.parseColor(paymentParams.view.toolbarBgColor)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                binding.toolbarTitle.setTextColor(
                    Color.parseColor(paymentParams.view.toolbarTextColor)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        initWebView()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.paymentState.collect {
                    Logger.i("Check state: $it")
                    val data = Intent().apply {
                        putExtra(EXTRA_INVOICE_ID, paymentParams.order.invoiceId)
                        putExtra(EXTRA_CODE_RESULT, it.requestCode)
                        putExtra(EXTRA_CODE_STATE, it.stateCode)
                        putExtra(EXTRA_ERROR_DESC, it.desc)
                        putExtra(EXTRA_ERROR, it.error)
                    }
                    when (it.stateCode) {
                        CheckPayStateCode.NOT_INITED,
                        CheckPayStateCode.INITED_NOT_PAYED,
                        CheckPayStateCode.PAYED_NOT_TRANSFERRED -> {
                            if (it.requestCode == CheckRequestCode.TIMEOUT_ERROR ||
                                it.requestCode == CheckRequestCode.SERVER_ERROR ||
                                it.requestCode == CheckRequestCode.SIGNATURE_ERROR ||
                                it.requestCode == CheckRequestCode.SHOP_ERROR ||
                                it.requestCode == CheckRequestCode.INVOICE_DOUBLE_ERROR ||
                                it.requestCode == CheckRequestCode.INVOICE_ZERO_ERROR) {
                                model.stopStatusTimer()
                                setResult(RESULT_FIRST_USER, data)
                                finish()
                            }
                        }
                        CheckPayStateCode.CANCELLED_NOT_PAYED -> {
                            model.stopStatusTimer()
                            setResult(RESULT_CANCELED, data)
                            finish()
                        }
                        CheckPayStateCode.PAYBACK,
                        CheckPayStateCode.STOPPED -> {
                            model.stopStatusTimer()
                            setResult(RESULT_FIRST_USER, data)
                            finish()
                        }
                        CheckPayStateCode.HOLD_SUCCESS,
                        CheckPayStateCode.SUCCESS -> {
                            model.stopStatusTimer()
                            setResult(RESULT_OK, data)
                            finish()
                        }
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebSettings() {
        CookieManager.getInstance().setAcceptCookie(true)
        val webSettings: WebSettings = binding.webView.settings
        webSettings.apply {
            cacheMode = LOAD_NORMAL
            domStorageEnabled = true
            javaScriptEnabled = true
            allowFileAccess = true
            allowFileAccessFromFileURLs = true
            allowContentAccess = true
            allowFileAccessFromFileURLs = true
        }
    }

    private fun initWebView() {
        configureWebSettings()
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                Logger.v("WebView shouldOverrideUrlLoading ${request?.url.toString()}")
                if (request?.url?.toString()
                        ?.startsWith("https://newbitrix.ht2.ipol.tech") == true
                ) {
                    model.initStatusTimer(paymentParams)
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Logger.v("WebView onPageStarted: $url")
            }

        }

        val urlParams = paymentParams.payPostParams(testMode)
        binding.webView.apply {
            postUrl(urlMain, EncodingUtils.getBytes(urlParams, "BASE64"))
        }

    }


}