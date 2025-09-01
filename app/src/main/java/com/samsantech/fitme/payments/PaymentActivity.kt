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
        val isUpgrade = intent.getBooleanExtra("isUpgrade", false)

        println("🔍 PaymentActivity Debug:")
        println("   - User: $user")
        println("   - Selected Plan: $selectedPlan")
        println("   - Selected Price: $selectedPrice")
        println("   - Is Upgrade: $isUpgrade")

        user?.let { data ->
            println("✅ User found: ID=${data.id}, Name=${data.fullName}")
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
            val paymentRequest = PaymentRequest(
                userId = data.id,
                plan = selectedPlan ?: "",
                amount = selectedPrice * 100,
                description = "Payment for $selectedPlan"
            )
            
            println("🔄 Initiating payment checkout for user: ${data.id}, plan: $selectedPlan, amount: $selectedPrice")
            
            RetrofitClient.payments.paymentCheckouts(paymentRequest)
                .enqueue(object : Callback<PaymentResponse> {
                    override fun onResponse(
                        call: Call<PaymentResponse?>,
                        response: Response<PaymentResponse?>
                    ) {
                        println("📡 Payment checkout response: ${response.code()}")
                        
                        if (response.isSuccessful) {
                            val res = response.body()
                            if (res != null && !res.checkoutUrl.isNullOrEmpty()) {
                                referenceNumber = res.referenceNumber
                                println("✅ Payment checkout successful, loading URL: ${res.checkoutUrl}")
                                webView.loadUrl(res.checkoutUrl)
                            } else {
                                println("❌ Payment checkout failed: Empty response body or checkout URL")
                                println("Response body: $res")
                                finish()
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            println("❌ Payment checkout failed: ${response.code()}")
                            println("Error body: $errorBody")
                            finish()
                        }
                    }

                    override fun onFailure(call: Call<PaymentResponse?>, t: Throwable) {
                        println("❌ Payment API error: ${t.message}")
                        t.printStackTrace()
                        finish()
                    }
                })
        } ?: run {
            println("❌ No user found in PaymentActivity - this should not happen!")
            println("   - Check if user was saved properly during registration")
            println("   - Check SharedPrefHelper.getLoggedInUser() implementation")
            finish()
        }
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)
    }
}
