package com.samsantech.fitme.auth

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.animation.ScaleAnimation
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

class PathSelectionActivity : AppCompatActivity() {

    private lateinit var memberCard: LinearLayout
    private lateinit var freeCard: LinearLayout

    private lateinit var checkboxLayoutFree: LinearLayout
    private lateinit var checkboxIconFree: ImageView
    private lateinit var checkboxTextFree: TextView
    private lateinit var buttonFreeStart: Button

    private var isAgreedFree = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_path_selection)

        memberCard = findViewById(R.id.cardMember)
        freeCard = findViewById(R.id.cardFree)
        checkboxLayoutFree = findViewById(R.id.privacyLayoutFree)
        checkboxIconFree = findViewById(R.id.checkboxIconFree)
        checkboxTextFree = findViewById(R.id.privacyCheckboxTextFree)
        buttonFreeStart = findViewById(R.id.buttonFreeStart)

        val fullName = intent.getStringExtra("fullName") ?: ""
        val username = intent.getStringExtra("username") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val password = intent.getStringExtra("password") ?: ""

        val fullText = "I agree to the Privacy Policy and Terms of Use"
        val span = SpannableString(fullText)
        val privacyStart = fullText.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length
        val termsStart = fullText.indexOf("Terms of Use")
        val termsEnd = termsStart + "Terms of Use".length

        span.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@PathSelectionActivity, PrivacyPolicyActivity::class.java))
            }
        }, privacyStart, privacyEnd, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

        span.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@PathSelectionActivity, TermsOfUseActivity::class.java))
            }
        }, termsStart, termsEnd, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

        span.setSpan(ForegroundColorSpan(Color.parseColor("#F97316")), privacyStart, privacyEnd, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(ForegroundColorSpan(Color.parseColor("#F97316")), termsStart, termsEnd, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

        checkboxTextFree.text = span
        checkboxTextFree.movementMethod = LinkMovementMethod.getInstance()

        memberCard.setOnClickListener {
            animateSelection(memberCard)
            resetCardStyle(freeCard)
            highlightCard(memberCard)

            val intent = Intent(this, MembershipPlanActivity::class.java)
            intent.putExtra("fullName", fullName)
            intent.putExtra("username", username)
            intent.putExtra("email", email)
            intent.putExtra("password", password)
            startActivity(intent)
        }

        freeCard.setOnClickListener {
            animateSelection(freeCard)
            resetCardStyle(memberCard)
            highlightCard(freeCard)

            checkboxLayoutFree.visibility = View.VISIBLE
            buttonFreeStart.visibility = View.VISIBLE
        }

        checkboxLayoutFree.setOnClickListener {
            isAgreedFree = !isAgreedFree
            checkboxIconFree.setImageResource(if (isAgreedFree) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked)
            buttonFreeStart.isEnabled = isAgreedFree
            buttonFreeStart.setBackgroundResource(if (isAgreedFree) R.drawable.rounded_button else R.drawable.rounded_button_disabled)
        }

        buttonFreeStart.setOnClickListener {
            val registerRequest = RegisterRequest(
                full_name = fullName,
                username = username,
                email = email,
                password = password,
                payment_method = "None"
            )

            RetrofitClient.auth.registerUser(registerRequest).enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    val user = response.body()?.user
                    val sharedPrefUsersInfo = getSharedPreferences("usersInfo", MODE_PRIVATE)
                    sharedPrefUsersInfo.edit().apply {
                        val gson = Gson()
                        val userJson = gson.toJson(user)
                        putString("user_data", userJson)
                        apply()
                    }
                    val intent = Intent(this@PathSelectionActivity, FreeSuccessActivity::class.java)
                    intent.putExtra("from", "no_membership")
                    startActivity(intent)
                    finish()
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    val intent = Intent(this@PathSelectionActivity, FreeSuccessActivity::class.java)
                    intent.putExtra("from", "no_membership")
                    startActivity(intent)
                    finish()
                }
            })
        }
    }

    private fun animateSelection(card: LinearLayout) {
        val anim = ScaleAnimation(0.97f, 1f, 0.97f, 1f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f)
        anim.duration = 120
        anim.fillAfter = true
        card.startAnimation(anim)
    }

    private fun highlightCard(card: LinearLayout) {
        card.setBackgroundResource(R.drawable.rounded_card_selected)
    }

    private fun resetCardStyle(card: LinearLayout) {
        card.setBackgroundResource(R.drawable.rounded_card)
    }
}