package com.samsantech.fitme.auth

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.samsantech.fitme.R
import com.samsantech.fitme.api.RetrofitClient
import com.samsantech.fitme.components.SharedPrefHelper
import com.samsantech.fitme.model.Plan
import com.samsantech.fitme.model.PlanResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MembershipUpgradeActivity : AppCompatActivity() {

    private lateinit var radioGroupPlans: RadioGroup
    private lateinit var nextButton: Button
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var retryButton: Button
    private lateinit var noPlansText: TextView
    private lateinit var plansContainer: RadioGroup
    
    private var selectedPlan: Plan? = null
    private var plans: List<Plan> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_membership_upgrade)

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
        radioGroupPlans = findViewById(R.id.radioGroupPlans)
        nextButton = findViewById(R.id.buttonNext)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        errorText = findViewById(R.id.errorText)
        retryButton = findViewById(R.id.retryButton)
        noPlansText = findViewById(R.id.noPlansText)
        plansContainer = findViewById(R.id.radioGroupPlans)

        // Initially disable next button
        nextButton.isEnabled = false

        // Back button
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Navigate to payment
        nextButton.setOnClickListener {
            selectedPlan?.let { plan ->
                val intent = Intent(this, PaymentMethodActivity::class.java)
                intent.putExtra("selectedPlan", plan.plan)
                intent.putExtra("selectedPrice", "₱${plan.price}")
                intent.putExtra("planId", plan.id)
                intent.putExtra("isUpgrade", true)
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "Select a plan first!", Toast.LENGTH_SHORT).show()
            }
        }

        // Retry button
        retryButton.setOnClickListener {
            fetchPlans()
        }

        // Fetch plans from API
        fetchPlans()
    }

    private fun fetchPlans() {
        showLoading(true)
        
        RetrofitClient.payments.getPlans().enqueue(object : Callback<PlanResponse> {
            override fun onResponse(call: Call<PlanResponse>, response: Response<PlanResponse>) {
                showLoading(false)
                
                if (response.isSuccessful) {
                    val planResponse = response.body()
                    if (planResponse?.plans != null) {
                        plans = planResponse.plans.filter { it.isActive == 1 }
                        if (plans.isNotEmpty()) {
                            populatePlans()
                        } else {
                            showNoPlans()
                        }
                    } else {
                        showNoPlans()
                    }
                } else {
                    showError("Failed to load plans: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PlanResponse>, t: Throwable) {
                showLoading(false)
                showError("Network error: ${t.message}")
            }
        })
    }

    private fun populatePlans() {
        plansContainer.removeAllViews()
        
        plans.forEach { plan ->
            val planView = createPlanView(plan)
            plansContainer.addView(planView)
        }
        
        plansContainer.visibility = View.VISIBLE
        noPlansText.visibility = View.GONE
    }

    private fun createPlanView(plan: Plan): View {
        val inflater = LayoutInflater.from(this)
        val planView = inflater.inflate(R.layout.item_plan_option, plansContainer, false)
        
        val container = planView.findViewById<LinearLayout>(R.id.planContainer)
        val radioButton = planView.findViewById<RadioButton>(R.id.planRadioButton)
        val planName = planView.findViewById<TextView>(R.id.planName)
        val planDuration = planView.findViewById<TextView>(R.id.planDuration)
        val planPrice = planView.findViewById<TextView>(R.id.planPrice)
        
        // Set plan data
        planName.text = plan.plan
        planDuration.text = "${plan.monthsCount} Month${if (plan.monthsCount > 1) "s" else ""}"
        planPrice.text = "₱${plan.price}"
        
        // Set click listener
        container.setOnClickListener {
            selectPlan(plan, radioButton, container)
        }
        
        return planView
    }

    private fun selectPlan(plan: Plan, radioButton: RadioButton, container: LinearLayout) {
        // Clear previous selection
        clearAllSelections()
        
        // Select new plan
        radioButton.isChecked = true
        selectedPlan = plan
        nextButton.isEnabled = true
        
        // Update background
        container.setBackgroundResource(R.drawable.plan_option_background_checked)
        radioButton.setButtonDrawable(R.drawable.ic_check_selected)
    }

    private fun clearAllSelections() {
        for (i in 0 until plansContainer.childCount) {
            val child = plansContainer.getChildAt(i)
            val container = child.findViewById<LinearLayout>(R.id.planContainer)
            val radioButton = child.findViewById<RadioButton>(R.id.planRadioButton)
            
            container.setBackgroundResource(R.drawable.plan_option_background)
            radioButton.isChecked = false
            radioButton.setButtonDrawable(R.drawable.ic_check_unselected)
        }
        
        selectedPlan = null
        nextButton.isEnabled = false
    }

    private fun showLoading(show: Boolean) {
        loadingIndicator.visibility = if (show) View.VISIBLE else View.GONE
        plansContainer.visibility = if (show) View.GONE else View.VISIBLE
        errorText.visibility = View.GONE
        retryButton.visibility = View.GONE
        noPlansText.visibility = View.GONE
    }

    private fun showError(message: String) {
        errorText.text = message
        errorText.visibility = View.VISIBLE
        retryButton.visibility = View.VISIBLE
        plansContainer.visibility = View.GONE
        loadingIndicator.visibility = View.GONE
        noPlansText.visibility = View.GONE
    }

    private fun showNoPlans() {
        noPlansText.visibility = View.VISIBLE
        plansContainer.visibility = View.GONE
        loadingIndicator.visibility = View.GONE
        errorText.visibility = View.GONE
        retryButton.visibility = View.GONE
    }
}
