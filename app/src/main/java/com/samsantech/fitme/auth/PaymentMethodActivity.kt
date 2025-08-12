package com.samsantech.fitme.auth

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.samsantech.fitme.R
import com.samsantech.fitme.api.RetrofitClient
import com.samsantech.fitme.info.PrivacyPolicyActivity
import com.samsantech.fitme.info.TermsOfUseActivity
import com.samsantech.fitme.model.RegisterRequest
import com.samsantech.fitme.model.RegisterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.graphics.toColorInt
import com.samsantech.fitme.model.ApiErrorResponse
import com.samsantech.fitme.payments.PaymentActivity

class PaymentMethodActivity : AppCompatActivity() {

    private lateinit var buttonPay: Button
    private lateinit var checkboxLayout: LinearLayout
    private lateinit var checkboxIcon: ImageView
    private var isAgreed = false
    private var selectedMethod: String = ""


    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_method)

        val fitMeText = findViewById<TextView>(R.id.textFitMe)
        val styledText = SpannableString("FitMe").apply {
            setSpan(ForegroundColorSpan("#F97316".toColorInt()), 0, 3, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan("#BEBEBE".toColorInt()), 3, 5, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        fitMeText.text = styledText

        val selectedPlanDisplayText = intent.getStringExtra("selectedPlan") ?: ""
        val selectedPlan = when (selectedPlanDisplayText) {
            "Starter Plan" -> "Starter"
            "Standard Plan" -> "Standard"
            "Pro Plan" -> "Pro"
            else -> selectedPlanDisplayText
        }

        val selectedPrice = intent.getStringExtra("selectedPrice")
        findViewById<TextView>(R.id.selectedPlanDisplay).text = "Membership Plan: $selectedPlanDisplayText – $selectedPrice"

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        buttonPay = findViewById(R.id.buttonPay)
        checkboxLayout = findViewById(R.id.privacyLayout)
        checkboxIcon = findViewById(R.id.checkboxIcon)
        val privacyTextView = findViewById<TextView>(R.id.privacyCheckboxText)

        checkboxLayout.visibility = View.GONE
        buttonPay.visibility = View.GONE
        buttonPay.isEnabled = false
        buttonPay.setBackgroundResource(R.drawable.rounded_button_disabled)

        val fullText = "I agree to the Privacy Policy and Terms of Use"
        val span = SpannableString(fullText)
        val privacyStart = fullText.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length
        val termsStart = fullText.indexOf("Terms of Use")
        val termsEnd = termsStart + "Terms of Use".length

        span.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@PaymentMethodActivity, PrivacyPolicyActivity::class.java))
            }
        }, privacyStart, privacyEnd, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

        span.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@PaymentMethodActivity, TermsOfUseActivity::class.java))
            }
        }, termsStart, termsEnd, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

        span.setSpan(ForegroundColorSpan("#F97316".toColorInt()), privacyStart, privacyEnd, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(ForegroundColorSpan("#F97316".toColorInt()), termsStart, termsEnd, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

        privacyTextView.text = span
        privacyTextView.movementMethod = LinkMovementMethod.getInstance()

        fun isFormValid(method: String): Boolean {
            if (!isAgreed) return false
            return when (method) {
                "GCASH" -> !findViewById<EditText?>(R.id.gcashNumber)?.text.isNullOrBlank()
                "CARD" -> listOf(
                    R.id.cardNumber,
                    R.id.cardName,
                    R.id.cardExpiry,
                    R.id.cardCvv
                ).mapNotNull { findViewById<EditText?>(it)?.text?.toString()?.trim() }.all { it.isNotEmpty() }
                "CASH" -> true
                "ONLINE_PAYMENT" -> true
                else -> false
            }
        }

        fun validateForm() {
            val canProceed = isFormValid(selectedMethod)
            buttonPay.isEnabled = canProceed
            buttonPay.setBackgroundResource(if (canProceed) R.drawable.rounded_button else R.drawable.rounded_button_disabled)
        }

        checkboxLayout.setOnClickListener {
            isAgreed = !isAgreed
            checkboxIcon.setImageResource(if (isAgreed) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked)
            validateForm()
        }

        val containerGCash = findViewById<LinearLayout>(R.id.containerGCash)
        val containerOnlinePayment = findViewById<LinearLayout>(R.id.containerOnlinePayment)
        val containerCard = findViewById<LinearLayout>(R.id.containerCard)
        val containerCash = findViewById<LinearLayout>(R.id.containerCOD)

        val radioGCash = findViewById<RadioButton>(R.id.paymentGCash)
        val onlinePaymentRadio = findViewById<RadioButton>(R.id.onlinePaymentRadio)
        val radioCard = findViewById<RadioButton>(R.id.paymentCard)
        val radioCash = findViewById<RadioButton>(R.id.paymentCOD)

        val paymentDetails = findViewById<LinearLayout>(R.id.paymentDetailsContainer)

        fun clearRadioButtons() {
            radioGCash.isChecked = false
            radioCard.isChecked = false
            radioCash.isChecked = false
            onlinePaymentRadio.isChecked = false
        }

        fun updateButtonLabel(method: String) {
            buttonPay.text = when (method) {
                "GCASH" -> "Proceed to GCash Payment"
                "CARD" -> "Proceed to Card Payment"
                "CASH" -> "Confirm and Pay at Front Desk"
                "ONLINE_PAYMENT" -> "Continue Online Payment "
                else -> "Continue"
            }
        }

        fun renderInputs(method: String) {
            selectedMethod = method
            paymentDetails.removeAllViews()
            paymentDetails.visibility = View.VISIBLE
            val viewToAdd = layoutInflater.inflate(
                when (method) {
                    "GCASH" -> R.layout.partial_gcash_form
                    "CARD" -> R.layout.partial_card_form
                    "CASH" -> R.layout.partial_cash_form
                    "ONLINE_PAYMENT" -> R.layout.partial_cash_form
                    else -> return
                }, paymentDetails, false
            )

            paymentDetails.addView(viewToAdd)
            checkboxLayout.alpha = 0f
            buttonPay.alpha = 0f
            checkboxLayout.visibility = View.VISIBLE
            buttonPay.visibility = View.VISIBLE
            checkboxLayout.animate().alpha(1f).setDuration(300).start()
            buttonPay.animate().alpha(1f).setDuration(300).start()

            isAgreed = false
            checkboxIcon.setImageResource(R.drawable.ic_checkbox_unchecked)
            buttonPay.isEnabled = false
            buttonPay.setBackgroundResource(R.drawable.rounded_button_disabled)

            if (viewToAdd is ViewGroup) {
                for (i in 0 until viewToAdd.childCount) {
                    val child = viewToAdd.getChildAt(i)
                    if (child is EditText) {
                        child.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable?) { validateForm() }
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        })
                    }
                }
            }
        }

        containerGCash.setOnClickListener {
            clearRadioButtons()
            radioGCash.isChecked = true
            renderInputs("GCASH")
            updateButtonLabel("GCASH")
        }
        containerOnlinePayment.setOnClickListener {
            clearRadioButtons()
            onlinePaymentRadio.isChecked = true
            renderInputs("ONLINE_PAYMENT")

            updateButtonLabel("ONLINE_PAYMENT")
        }

        containerCard.setOnClickListener {
            clearRadioButtons()
            radioCard.isChecked = true
            renderInputs("CARD")
            updateButtonLabel("CARD")
        }

        containerCash.setOnClickListener {
            clearRadioButtons()
            radioCash.isChecked = true
            renderInputs("CASH")
            updateButtonLabel("CASH")
        }

        buttonPay.setOnClickListener {
            val fullName = intent.getStringExtra("fullName") ?: ""
            val username = intent.getStringExtra("username") ?: ""
            val email = intent.getStringExtra("email") ?: ""
            val password = intent.getStringExtra("password") ?: ""
            val selectedPrice = intent.getStringExtra("selectedPrice")?.replace("₱", "")?.replace(",", "") ?: ""

            val priceInt = selectedPrice.toIntOrNull() ?: 0

            if (priceInt == 0) {
                Toast.makeText(this, "Invalid price. Cannot proceed.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val gcashNumber: String? = findViewById<EditText?>(R.id.gcashNumber)?.text?.toString()?.trim()
            val gcashName: String? = findViewById<EditText?>(R.id.gcashName)?.text?.toString()?.trim()
            val cardNumber: String? = findViewById<EditText?>(R.id.cardNumber)?.text?.toString()?.trim()
            val cardName: String? = findViewById<EditText?>(R.id.cardName)?.text?.toString()?.trim()
            val cardExpiry: String? = findViewById<EditText?>(R.id.cardExpiry)?.text?.toString()?.trim()
            val cardCvv: String? = findViewById<EditText?>(R.id.cardCvv)?.text?.toString()?.trim()

            val request = RegisterRequest(
                full_name = fullName,
                username = username,
                email = email,
                password = password,
                plan = selectedPlan,
                price = priceInt,
                payment_method = selectedMethod,
                gcash_number = if (selectedMethod == "GCASH") gcashNumber else null,
                gcash_name = if (selectedMethod == "GCASH" && !gcashName.isNullOrBlank()) gcashName else null,
                card_number = if (selectedMethod == "CARD") cardNumber else null,
                card_name = if (selectedMethod == "CARD") cardName else null,
                card_expiry = if (selectedMethod == "CARD") cardExpiry else null,
                card_cvv = if (selectedMethod == "CARD") cardCvv else null
            )

            Log.d("REGISTER_DEBUG", Gson().toJson(request))
            RetrofitClient.auth.registerUser(request).enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val user = response.body()?.user
                        val sharedPrefUsersInfo = getSharedPreferences("usersInfo",MODE_PRIVATE)
                        sharedPrefUsersInfo.edit().apply {
                            val gson = Gson()
                            val userJson = gson.toJson(user)
                            putString("user_data", userJson)
                            apply()
                        }
                        val intent = when (selectedMethod) {
                            "CASH" -> Intent(this@PaymentMethodActivity, CashPendingActivity::class.java)
                            "ONLINE_PAYMENT" -> Intent(this@PaymentMethodActivity, PaymentActivity::class.java)
                            else -> Intent(this@PaymentMethodActivity, SuccessActivity::class.java)
                        }
                        intent.putExtra("selectedPrice", priceInt)
                        intent.putExtra("selectedPlan", selectedPlan)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorBody = response.errorBody()
                        val errorString = errorBody?.string()

                        val gson = Gson()
                        val apiError = gson.fromJson(errorString, ApiErrorResponse::class.java)
                        val errorMsg = apiError.error?.sqlMessage ?: apiError.message

                        Toast.makeText(this@PaymentMethodActivity, errorMsg, Toast.LENGTH_LONG).show()
                    } }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    Toast.makeText(this@PaymentMethodActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}