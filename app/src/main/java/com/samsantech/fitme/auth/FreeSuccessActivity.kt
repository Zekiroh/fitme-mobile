package com.samsantech.fitme.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.samsantech.fitme.R
import com.samsantech.fitme.onboarding.AssessmentGenderActivity

class FreeSuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_free_success)

        val getStartedButton = findViewById<Button>(R.id.buttonGetStarted)
        getStartedButton.setOnClickListener {
            startActivity(Intent(this, AssessmentGenderActivity::class.java))
            finish()
        }
    }
}
