package com.samsantech.fitme.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.samsantech.fitme.R
import com.samsantech.fitme.main.MainActivity

class AssessmentFrequencyActivity : AppCompatActivity() {

    private lateinit var optionBeginner: LinearLayout
    private lateinit var optionIntermediate: LinearLayout
    private lateinit var optionAdvanced: LinearLayout

    private lateinit var btnNext: Button
    private lateinit var skipText: TextView
    private lateinit var backButton: ImageView

    private var selectedFrequency = ""
    private var selectedGender = ""
    private var selectedGoal = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assessment_frequency)

        selectedGender = intent.getStringExtra("gender") ?: ""
        selectedGoal = intent.getStringExtra("goal") ?: ""

        optionBeginner = findViewById(R.id.optionBeginner)
        optionIntermediate = findViewById(R.id.optionIntermediate)
        optionAdvanced = findViewById(R.id.optionAdvanced)

        btnNext = findViewById(R.id.btnNextFrequency)
        skipText = findViewById(R.id.skipText)
        backButton = findViewById(R.id.backButton)

        optionBeginner.setOnClickListener {
            selectedFrequency = "Beginner"
            highlightSelected(optionBeginner)
            resetOthers(optionIntermediate, optionAdvanced)
        }

        optionIntermediate.setOnClickListener {
            selectedFrequency = "Intermediate"
            highlightSelected(optionIntermediate)
            resetOthers(optionBeginner, optionAdvanced)
        }

        optionAdvanced.setOnClickListener {
            selectedFrequency = "Advanced"
            highlightSelected(optionAdvanced)
            resetOthers(optionBeginner, optionIntermediate)
        }

        btnNext.setOnClickListener {
            val intent = Intent(this, AssessmentWeightActivity::class.java)
            intent.putExtra("gender", selectedGender)
            intent.putExtra("goal", selectedGoal)
            intent.putExtra("frequency", selectedFrequency)
            startActivity(intent)
        }

        skipText.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("gender", selectedGender)
            intent.putExtra("goal", selectedGoal)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun highlightSelected(container: LinearLayout) {
        resetAllOptions()
        container.isSelected = true
        container.setBackgroundResource(R.drawable.selector_goal_option)

        val white = ContextCompat.getColor(this, android.R.color.white)
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is TextView) {
                child.setTextColor(white)
            }
        }

        btnNext.isEnabled = true
        btnNext.alpha = 1f
    }

    private fun resetOthers(c1: LinearLayout, c2: LinearLayout) {
        val orange = ContextCompat.getColor(this, R.color.orange_500)

        listOf(c1, c2).forEach { container ->
            container.isSelected = false
            container.setBackgroundResource(R.drawable.selector_goal_option)
            for (i in 0 until container.childCount) {
                val child = container.getChildAt(i)
                if (child is TextView) {
                    child.setTextColor(orange)
                }
            }
        }
    }

    private fun resetAllOptions() {
        resetOthers(optionBeginner, optionIntermediate)
        resetOthers(optionIntermediate, optionAdvanced)
    }
}
