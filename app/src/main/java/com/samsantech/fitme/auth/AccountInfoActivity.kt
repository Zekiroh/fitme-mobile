package com.samsantech.fitme.auth

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.text.Editable
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.samsantech.fitme.R

class AccountInfoActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_info)

        // FitMe branding
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

        // Input Fields
        val fullNameInput = findViewById<EditText>(R.id.editTextFullName)
        val usernameInput = findViewById<EditText>(R.id.editTextUsername)
        val emailInput = findViewById<EditText>(R.id.editTextEmail)
        val passwordInput = findViewById<EditText>(R.id.editTextPassword)
        val confirmPasswordInput = findViewById<EditText>(R.id.editTextConfirmPassword)
        val passwordToggle = findViewById<ImageView>(R.id.passwordToggle)
        val confirmPasswordToggle = findViewById<ImageView>(R.id.confirmPasswordToggle)
        val nextButton = findViewById<Button>(R.id.buttonNext)

        // Button starts disabled
        nextButton.isEnabled = false

        // Real-time enable logic
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val allFilled = fullNameInput.text.isNotEmpty() &&
                        usernameInput.text.isNotEmpty() &&
                        emailInput.text.isNotEmpty() &&
                        passwordInput.text.isNotEmpty() &&
                        confirmPasswordInput.text.isNotEmpty()

                val passwordsMatch = passwordInput.text.toString() == confirmPasswordInput.text.toString()

                nextButton.isEnabled = allFilled && passwordsMatch
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        // Attach to all inputs
        fullNameInput.addTextChangedListener(watcher)
        usernameInput.addTextChangedListener(watcher)
        emailInput.addTextChangedListener(watcher)
        passwordInput.addTextChangedListener(watcher)
        confirmPasswordInput.addTextChangedListener(watcher)

        // Toggle visibility
        passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            passwordInput.inputType = if (isPasswordVisible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            passwordInput.typeface = Typeface.DEFAULT
            passwordInput.setSelection(passwordInput.text.length)
            passwordToggle.setImageResource(
                if (isPasswordVisible) R.drawable.ic_eye_hide else R.drawable.ic_eye_show
            )
        }

        confirmPasswordToggle.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            confirmPasswordInput.inputType = if (isConfirmPasswordVisible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            confirmPasswordInput.typeface = Typeface.DEFAULT
            confirmPasswordInput.setSelection(confirmPasswordInput.text.length)
            confirmPasswordToggle.setImageResource(
                if (isConfirmPasswordVisible) R.drawable.ic_eye_hide else R.drawable.ic_eye_show
            )
        }

        // Redirect to Login screen
        val loginText = findViewById<TextView>(R.id.textLogin)
        val loginSpannable = SpannableString(" Login")
        loginSpannable.setSpan(UnderlineSpan(), 0, loginSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        loginText.text = loginSpannable
        loginText.setOnClickListener {
            startActivity(Intent(this@AccountInfoActivity, LoginActivity::class.java))
            finish()
        }

        // Next button logic to Page 2
        nextButton.setOnClickListener {
            val fullName = fullNameInput.text.toString().trim()
            val username = usernameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            val intent = Intent(this, PathSelectionActivity::class.java)
            intent.putExtra("fullName", fullName)
            intent.putExtra("username", username)
            intent.putExtra("email", email)
            intent.putExtra("password", password)
            startActivity(intent)
        }
    }
}
