package com.samsantech.fitme.info

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.text.Spannable
import android.text.SpannableString
import android.graphics.Color
import android.text.style.ForegroundColorSpan
import com.samsantech.fitme.R
import com.samsantech.fitme.api.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PasswordRecoveryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_recovery)

        // FitMe colored branding
        val fitMeText = findViewById<TextView>(R.id.textFitMe)
        val styledText = SpannableString("FitMe")
        styledText.setSpan(
            ForegroundColorSpan(Color.parseColor("#F97316")), 0, 3,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        styledText.setSpan(
            ForegroundColorSpan(Color.parseColor("#BEBEBE")), 3, 5,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        fitMeText.text = styledText

        // Handle submit
        val emailInput = findViewById<EditText>(R.id.editTextRecoveryEmail)
        val submitButton = findViewById<Button>(R.id.buttonSubmitRecovery)

        submitButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show()
            } else {
                RetrofitClient.auth.sendResetEmail(email)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@PasswordRecoveryActivity, "Check your email for reset instructions.", Toast.LENGTH_LONG).show()
                                finish() // go back to login
                            } else {
                                Toast.makeText(this@PasswordRecoveryActivity, "Failed to send reset email.", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            Toast.makeText(this@PasswordRecoveryActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
            }
        }

        // Back button
        val backButton = findViewById<ImageView>(R.id.buttonBack)
        backButton.setOnClickListener {
            onBackPressed()
        }
    }
}
