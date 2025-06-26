package com.samsantech.fitme.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.samsantech.fitme.R
import com.samsantech.fitme.onboarding.AssessmentGenderActivity

class SuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)

        findViewById<Button>(R.id.btnGoHome).setOnClickListener {
            // Redirect to Initial Fitness Assessment Page
            val intent = Intent(this, AssessmentGenderActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
