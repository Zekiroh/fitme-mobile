package com.samsantech.fitme.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.samsantech.fitme.R
import com.samsantech.fitme.main.MainActivity
import com.samsantech.fitme.onboarding.AssessmentGenderActivity

class SuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)

        val isUpgrade = intent.getBooleanExtra("isUpgrade", false)
        val selectedPlan = intent.getStringExtra("selectedPlan") ?: ""
        val selectedPrice = intent.getIntExtra("selectedPrice", 0)
        val refreshMembership = intent.getBooleanExtra("refreshMembership", false)

        // Update UI based on whether it's registration or upgrade
        val titleText = findViewById<TextView>(R.id.titleText)
        val descriptionText = findViewById<TextView>(R.id.descriptionText)
        val actionButton = findViewById<Button>(R.id.btnGoHome)

        if (isUpgrade) {
            // Membership upgrade success
            titleText.text = "Membership Upgraded!"
            descriptionText.text = "Your membership has been successfully upgraded to $selectedPlan. You can now enjoy all the premium features!"
            actionButton.text = "Go to Profile"
            
            actionButton.setOnClickListener {
                // Navigate to MainActivity (which will show the profile)
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                if (refreshMembership) {
                    // Signal that membership details should be refreshed
                    intent.putExtra("refreshMembershipDetails", true)
                }
                startActivity(intent)
            }
        } else {
            // Registration success
            titleText.text = "Payment Successful!"
            descriptionText.text = "Your FitMe account has been created and your membership is now active. Let's get started with your fitness journey!"
            actionButton.text = "Get Started"
            
            actionButton.setOnClickListener {
                // Redirect to Initial Fitness Assessment Page
                val intent = Intent(this, AssessmentGenderActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}
