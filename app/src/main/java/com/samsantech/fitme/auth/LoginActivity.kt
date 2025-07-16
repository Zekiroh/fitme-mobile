package com.samsantech.fitme.auth

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.samsantech.fitme.R
import com.samsantech.fitme.api.RetrofitClient
import com.samsantech.fitme.model.LoginRequest
import com.samsantech.fitme.model.LoginResponse
import com.samsantech.fitme.onboarding.AssessmentGenderActivity
import com.samsantech.fitme.info.PasswordRecoveryActivity
import com.samsantech.fitme.main.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.json.JSONObject
import androidx.core.content.edit

class LoginActivity : AppCompatActivity() {
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val fitMeText = findViewById<TextView>(R.id.textFitMe)
        val styledText = SpannableString("FitMe")
        styledText.setSpan(
            ForegroundColorSpan(Color.parseColor("#FF7F50")),
            0, 3,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        fitMeText.text = styledText

        val emailInput = findViewById<EditText>(R.id.editTextEmail)
        val passwordInput = findViewById<EditText>(R.id.editTextPassword)
        val passwordToggle = findViewById<ImageView>(R.id.passwordToggle)
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val signUpLink = findViewById<TextView>(R.id.textSignUp)
        val forgotPasswordText = findViewById<TextView>(R.id.textForgotPassword)

        signUpLink.paintFlags = signUpLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        forgotPasswordText.paintFlags = forgotPasswordText.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        signUpLink.setOnClickListener {
            val intent = Intent(this, AccountInfoActivity::class.java)
            startActivity(intent)
        }

        forgotPasswordText.setOnClickListener {
            val intent = Intent(this, PasswordRecoveryActivity::class.java)
            startActivity(intent)
        }

        passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            passwordInput.inputType = if (isPasswordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            passwordToggle.setImageResource(if (isPasswordVisible) R.drawable.ic_eye_hide else R.drawable.ic_eye_show)
            passwordInput.typeface = Typeface.DEFAULT
            passwordInput.setSelection(passwordInput.text.length)
        }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password are required.", Toast.LENGTH_SHORT).show()
            } else {
                val loginRequest = LoginRequest(email, password)

                RetrofitClient.auth.loginUser(loginRequest).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        println(response.toString())
                        if (response.isSuccessful) {
                            val user = response.body()?.user

                            if (user != null) {
                                // Save to SharedPreferences
                                val sharedPref = getSharedPreferences("FitMePrefs", Context.MODE_PRIVATE)
                                sharedPref.edit().apply {
                                    putInt("user_id", user.id)
                                    putString("full_name", user.fullName)
                                    apply()
                                }
                                val sharedPrefUsersInfo = getSharedPreferences("usersInfo", Context.MODE_PRIVATE)
                                sharedPrefUsersInfo.edit().apply {
                                    val gson = Gson()
                                    val userJson = gson.toJson(user)
                                    putString("user_data", userJson)
                                    apply()
                                }

                                Toast.makeText(this@LoginActivity, "Welcome ${user.fullName}", Toast.LENGTH_SHORT).show()
                                if(!user.frequency.isNullOrEmpty()) {
                                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                } else {
                                    startActivity(Intent(this@LoginActivity, AssessmentGenderActivity::class.java))
                                }
                                // Proceed to Assessment or Dashboard
                                finish()
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            val message = try {
                                JSONObject(errorBody).getString("message")
                            } catch (e: Exception) {
                                "Invalid credentials or access denied"
                            }
                            Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }
    }
}