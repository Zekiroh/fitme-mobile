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
                Toast.makeText(this, "Recovery link sent to $email", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        // Back button
        val backButton = findViewById<ImageView>(R.id.buttonBack)
        backButton.setOnClickListener {
            onBackPressed()
        }
    }
}
