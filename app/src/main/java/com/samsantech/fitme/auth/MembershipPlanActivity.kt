package com.samsantech.fitme.auth

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.content.Intent
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.samsantech.fitme.R

class MembershipPlanActivity : AppCompatActivity() {

    private lateinit var planBasic: RadioButton
    private lateinit var planStandard: RadioButton
    private lateinit var planPremium: RadioButton

    private lateinit var containerBasic: LinearLayout
    private lateinit var containerStandard: LinearLayout
    private lateinit var containerPremium: LinearLayout

    private lateinit var nextButton: Button
    private var selectedPlan: RadioButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_membership_plan)

        // Apply FitMe branding
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

        // Bind views
        planBasic = findViewById(R.id.planBasic)
        planStandard = findViewById(R.id.planStandard)
        planPremium = findViewById(R.id.planPremium)

        containerBasic = planBasic.parent as LinearLayout
        containerStandard = planStandard.parent as LinearLayout
        containerPremium = planPremium.parent as LinearLayout

        nextButton = findViewById(R.id.buttonNext)
        nextButton.isEnabled = false // initially disabled

        resetIcons()

        // Plan block click listeners
        setupPlanClick(containerBasic, planBasic)
        setupPlanClick(containerStandard, planStandard)
        setupPlanClick(containerPremium, planPremium)

        // Back button
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Navigate to payment
        nextButton.setOnClickListener {
            val selectedId = selectedPlan?.id

            val plan = when (selectedId) {
                R.id.planBasic -> "Starter"
                R.id.planStandard -> "Standard"
                R.id.planPremium -> "Pro"
                else -> null
            }

            val price = when (selectedId) {
                R.id.planBasic -> "₱1,500"
                R.id.planStandard -> "₱6,000"
                R.id.planPremium -> "₱8,000"
                else -> null
            }

            val fullName = intent.getStringExtra("fullName") ?: ""
            val username = intent.getStringExtra("username") ?: ""
            val email = intent.getStringExtra("email") ?: ""
            val password = intent.getStringExtra("password") ?: ""

            if (plan != null && price != null) {
                val intent = Intent(this, PaymentMethodActivity::class.java)
                intent.putExtra("selectedPlan", plan)
                intent.putExtra("selectedPrice", price)
                intent.putExtra("fullName", fullName)
                intent.putExtra("username", username)
                intent.putExtra("email", email)
                intent.putExtra("password", password)
                startActivity(intent)
            } else {
                toast("Select a plan first!")
            }
        }
    }

    private fun setupPlanClick(container: LinearLayout, radioButton: RadioButton) {
        container.setOnClickListener {
            clearAllSelections()
            radioButton.isChecked = true
            selectedPlan = radioButton
            nextButton.isEnabled = true // enable after selection

            when (radioButton.id) {
                R.id.planBasic -> containerBasic.setBackgroundResource(R.drawable.plan_option_background_checked)
                R.id.planStandard -> containerStandard.setBackgroundResource(R.drawable.plan_option_background_checked)
                R.id.planPremium -> containerPremium.setBackgroundResource(R.drawable.plan_option_background_checked)
            }

            radioButton.setButtonDrawable(R.drawable.ic_check_selected)
        }
    }

    private fun clearAllSelections() {
        val unselected = R.drawable.ic_check_unselected

        planBasic.isChecked = false
        planBasic.setButtonDrawable(unselected)
        containerBasic.setBackgroundResource(R.drawable.plan_option_background)

        planStandard.isChecked = false
        planStandard.setButtonDrawable(unselected)
        containerStandard.setBackgroundResource(R.drawable.plan_option_background)

        planPremium.isChecked = false
        planPremium.setButtonDrawable(unselected)
        containerPremium.setBackgroundResource(R.drawable.plan_option_background)

        selectedPlan = null
        nextButton.isEnabled = false // disable again if user deselects all
    }

    private fun resetIcons() {
        planBasic.setButtonDrawable(R.drawable.ic_check_unselected)
        planStandard.setButtonDrawable(R.drawable.ic_check_unselected)
        planPremium.setButtonDrawable(R.drawable.ic_check_unselected)
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}