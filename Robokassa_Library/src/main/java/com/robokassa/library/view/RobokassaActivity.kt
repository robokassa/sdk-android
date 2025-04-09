package com.robokassa.library.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebSettings.LOAD_NORMAL
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
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
import com.robokassa.library.EXTRA_OP_KEY
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
import com.robokassa.library.urlSaving
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class RobokassaActivity : AppCompatActivity() {

    companion object {
        fun intent(options: PaymentParams, testMode: Boolean, context: Context): Intent {
            val intent = Intent(context, RobokassaActivity::class.java)
            intent.putExtra(EXTRA_PARAMS, options)
            intent.putExtra(EXTRA_TEST_PARAMETERS, testMode)
            return intent
        }
    }

    private lateinit var binding: ActivityRobokassaBinding
    private lateinit var paymentParams: PaymentParams
    private var testMode = false

    private val rotate1 = RotateAnimation(
        0f, 359f,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f
    ).apply {
        duration = 2800
        repeatCount = Animation.INFINITE
    }
    private val rotate2 = RotateAnimation(
        0f, 359f,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f
    ).apply {
        duration = 2400
        repeatCount = Animation.INFINITE
    }
    private val rotate3 = RotateAnimation(
        0f, 359f,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f
    ).apply {
        duration = 2200
        repeatCount = Animation.INFINITE
    }

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
            Toast.makeText(this, "Payment params not set", Toast.LENGTH_LONG).show()
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
        initProgressAnimation()
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
                        putExtra(EXTRA_OP_KEY, it.opKey)
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
                                it.requestCode == CheckRequestCode.INVOICE_ZERO_ERROR
                            ) {
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

                        CheckPayStateCode.PAYMENT_PAYBACK,
                        CheckPayStateCode.PAYMENT_STOPPED -> {
                            model.stopStatusTimer()
                            setResult(RESULT_FIRST_USER, data)
                            finish()
                        }

                        CheckPayStateCode.HOLD_SUCCESS,
                        CheckPayStateCode.PAYMENT_SUCCESS -> {
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
                return if (checkUrl(request?.url?.toString())) {
                    model.initStatusTimer(paymentParams)
                    true
                } else if (checkWebLinks(request?.url?.toString())) {
                    super.shouldOverrideUrlLoading(view, request)
                } else {
                    try {
                        val i = if (request?.url?.toString()?.startsWith("intent://") == true) {
                            Intent.parseUri(request.url?.toString(), Intent.URI_INTENT_SCHEME)
                        } else {
                            Intent(Intent.ACTION_VIEW, request?.url)
                        }
                        startActivity(i)
                        true
                    } catch (e: Exception) {
                        Toast.makeText(this@RobokassaActivity, "No apps to open", Toast.LENGTH_LONG).show()
                        true
                    }
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Logger.v("WebView shouldOverrideUrlLoading ${url.toString()}")
                return if (checkUrl(url)) {
                    model.initStatusTimer(paymentParams)
                    true
                } else if (checkWebLinks(url)) {
                    super.shouldOverrideUrlLoading(view, url)
                } else {
                    try {
                        val i = if (url?.startsWith("intent://") == true) {
                            Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        } else {
                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        }
                        startActivity(i)
                        true
                    } catch (e: Exception) {
                        Toast.makeText(this@RobokassaActivity, "No apps to open d", Toast.LENGTH_LONG).show()
                        true
                    }
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Logger.v("WebView onPageStarted $url")
                if (checkUrl(url)) {
                    binding.webView.isInvisible = true
                    binding.progress.isInvisible = false
                    model.initStatusTimer(paymentParams)
                } else {
                    binding.webView.isInvisible = false
                    binding.progress.isInvisible = true
                    super.onPageStarted(view, url, favicon)
                }
            }

        }

        val urlParams = paymentParams.payPostParams(testMode)
        binding.webView.apply {
            postUrl(
                if (paymentParams.order.token.isNullOrEmpty()) urlMain else urlSaving,
                EncodingUtils.getBytes(urlParams, "BASE64")
            )
        }

    }

    private fun checkUrl(url: String?): Boolean {
        return url?.startsWith(
            paymentParams.redirectUrl
        ) == true || url?.startsWith(
            "https://auth.robokassa.ru/Merchant/State/"
        ) == true || url?.contains("ipol.tech/") == true || url?.contains("ipol.ru/") == true
    }

    private fun checkWebLinks(url: String?): Boolean {
        return url != null && (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url))
    }

    private fun initProgressAnimation() {
        binding.progressStroke.startAnimation(rotate1)
        binding.progressCircle.startAnimation(rotate2)
        binding.progressLogo.startAnimation(rotate3)
    }
}
