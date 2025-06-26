package com.samsantech.fitme.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.samsantech.fitme.R
import com.samsantech.fitme.main.MainActivity

class AssessmentGoalActivity : AppCompatActivity() {

    private lateinit var optionMuscle: LinearLayout
    private lateinit var optionFit: LinearLayout
    private lateinit var optionWeight: LinearLayout

    private lateinit var muscleIcon: ImageView
    private lateinit var fitIcon: ImageView
    private lateinit var weightIcon: ImageView

    private lateinit var muscleText: TextView
    private lateinit var fitText: TextView
    private lateinit var weightText: TextView

    private lateinit var muscleSubtext: TextView
    private lateinit var fitSubtext: TextView
    private lateinit var weightSubtext: TextView

    private lateinit var btnNext: Button
    private lateinit var skipText: TextView
    private lateinit var backButton: ImageView

    private var selectedGoal = ""
    private var selectedGender = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assessment_goal)

        selectedGender = intent.getStringExtra("gender") ?: ""

        // Initialize views
        optionMuscle = findViewById(R.id.optionMuscle)
        optionFit = findViewById(R.id.optionFit)
        optionWeight = findViewById(R.id.optionWeight)

        muscleIcon = findViewById(R.id.muscleIcon)
        fitIcon = findViewById(R.id.fitIcon)
        weightIcon = findViewById(R.id.weightIcon)

        muscleText = findViewById(R.id.muscleText)
        fitText = findViewById(R.id.fitText)
        weightText = findViewById(R.id.weightText)

        muscleSubtext = findViewById(R.id.muscleSubtext)
        fitSubtext = findViewById(R.id.fitSubtext)
        weightSubtext = findViewById(R.id.weightSubtext)

        btnNext = findViewById(R.id.btnNextGoal)
        skipText = findViewById(R.id.skipText)
        backButton = findViewById(R.id.backButton)

        // Click listeners
        optionMuscle.setOnClickListener {
            selectedGoal = "Build Muscle"
            highlightSelected(optionMuscle, muscleText, muscleSubtext)
            resetOthers(optionFit, fitText, fitSubtext, optionWeight, weightText, weightSubtext)
        }

        optionFit.setOnClickListener {
            selectedGoal = "Stay Fit"
            highlightSelected(optionFit, fitText, fitSubtext)
            resetOthers(optionMuscle, muscleText, muscleSubtext, optionWeight, weightText, weightSubtext)
        }

        optionWeight.setOnClickListener {
            selectedGoal = "Lose Weight"
            highlightSelected(optionWeight, weightText, weightSubtext)
            resetOthers(optionMuscle, muscleText, muscleSubtext, optionFit, fitText, fitSubtext)
        }

        btnNext.setOnClickListener {
            val intent = Intent(this, AssessmentFrequencyActivity::class.java)
            intent.putExtra("gender", selectedGender)
            intent.putExtra("goal", selectedGoal)
            startActivity(intent)
        }

        skipText.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("gender", selectedGender)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun highlightSelected(container: LinearLayout, text: TextView, subtext: TextView) {
        resetAllOptions()
        container.isSelected = true
        container.setBackgroundResource(R.drawable.selector_goal_option)
        text.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        subtext.visibility = TextView.VISIBLE
        btnNext.isEnabled = true
        btnNext.alpha = 1f
    }

    private fun resetOthers(
        c1: LinearLayout, t1: TextView, s1: TextView,
        c2: LinearLayout, t2: TextView, s2: TextView
    ) {
        c1.isSelected = false
        c2.isSelected = false

        c1.setBackgroundResource(R.drawable.selector_goal_option)
        c2.setBackgroundResource(R.drawable.selector_goal_option)

        val orange = ContextCompat.getColor(this, R.color.orange_500)
        t1.setTextColor(orange)
        t2.setTextColor(orange)

        s1.visibility = TextView.GONE
        s2.visibility = TextView.GONE
    }

    private fun resetAllOptions() {
        optionMuscle.isSelected = false
        optionFit.isSelected = false
        optionWeight.isSelected = false

        optionMuscle.setBackgroundResource(R.drawable.selector_goal_option)
        optionFit.setBackgroundResource(R.drawable.selector_goal_option)
        optionWeight.setBackgroundResource(R.drawable.selector_goal_option)

        muscleText.setTextColor(ContextCompat.getColor(this, R.color.orange_500))
        fitText.setTextColor(ContextCompat.getColor(this, R.color.orange_500))
        weightText.setTextColor(ContextCompat.getColor(this, R.color.orange_500))

        muscleSubtext.visibility = TextView.GONE
        fitSubtext.visibility = TextView.GONE
        weightSubtext.visibility = TextView.GONE
    }
}
