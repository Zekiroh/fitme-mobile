package com.samsantech.fitme.payments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.samsantech.fitme.R
import com.samsantech.fitme.api.RetrofitClient
import com.samsantech.fitme.auth.SuccessActivity
import com.samsantech.fitme.components.SharedPrefHelper
import com.samsantech.fitme.model.PaymentRequest
import com.samsantech.fitme.model.PaymentRequestCallback
import com.samsantech.fitme.model.PaymentResponse
import com.samsantech.fitme.model.PaymentStatusResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentActivity : AppCompatActivity() {

    private lateinit var referenceNumber: String
    private var paymentHandled = false // prevent multiple triggers

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_view)

        val user = SharedPrefHelper.getLoggedInUser(this)
        val webView: WebView = findViewById(R.id.webView)
        val selectedPlan = intent.getStringExtra("selectedPlan")
        val selectedPrice = intent.getIntExtra("selectedPrice", 0)

        user?.let { data ->
            println(data.id)
            // Set up WebView
            webView.settings.javaScriptEnabled = true
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (url == "https://fitmegym.com/" && ::referenceNumber.isInitialized && !paymentHandled) {
                        paymentHandled = true // ensure callback is triggered only once

                        RetrofitClient.payments.paymentCallBack(
                            data.id,
                            PaymentRequestCallback(referenceNumber = referenceNumber)
                        ).enqueue(object : Callback<PaymentStatusResponse> {
                            override fun onResponse(
                                call: Call<PaymentStatusResponse?>,
                                response: Response<PaymentStatusResponse?>
                            ) {
                                val result = response.body()
//
                                if (response.isSuccessful && result?.success == true) {
                                    startActivity(Intent(this@PaymentActivity, SuccessActivity::class.java))
                                    finish()
                                } else {
                                    // ❌ Show error or finish
                                    startActivity(Intent(this@PaymentActivity, FailedActivity::class.java))
                                    finish()
                                }
                            }

                            override fun onFailure(call: Call<PaymentStatusResponse?>, t: Throwable) {
                                println("❌ Callback failed: ${t.message}")
                                finish()
                            }
                        })
                    }
                }
            }

            // Request payment and load checkout URL
            RetrofitClient.payments.paymentCheckouts(
                PaymentRequest(
                    userId = data.id,
                    plan = selectedPlan?:"",
                    amount = selectedPrice * 100,
                    description = "Payment for Starter Plan"
                )
            ).enqueue(object : Callback<PaymentResponse> {
                override fun onResponse(
                    call: Call<PaymentResponse?>,
                    response: Response<PaymentResponse?>
                ) {
                    val res = response.body()
                    if (response.isSuccessful && res != null) {
                        referenceNumber = res.referenceNumber
                        webView.loadUrl(res.checkoutUrl)
                    } else {
                        println("❌ Payment checkout failed")
                        finish()
                    }
                }

                override fun onFailure(call: Call<PaymentResponse?>, t: Throwable) {
                    println("❌ Payment API error: ${t.message}")
                    finish()
                }
            })
        } ?: finish() // If user is null, close activity
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)
    }
}
