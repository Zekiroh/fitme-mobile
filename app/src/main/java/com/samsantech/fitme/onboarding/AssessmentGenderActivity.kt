package com.samsantech.fitme.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.samsantech.fitme.R
import com.samsantech.fitme.main.MainActivity

class AssessmentGenderActivity : AppCompatActivity() {

    private lateinit var optionMale: LinearLayout
    private lateinit var optionFemale: LinearLayout
    private lateinit var maleCircle: FrameLayout
    private lateinit var femaleCircle: FrameLayout
    private lateinit var maleIcon: ImageView
    private lateinit var femaleIcon: ImageView
    private lateinit var btnNext: Button
    private lateinit var skipText: TextView

    private var selectedGender = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assessment_gender)

        // Initialize views
        optionMale = findViewById(R.id.optionMale)
        optionFemale = findViewById(R.id.optionFemale)
        maleCircle = findViewById(R.id.maleCircle)
        femaleCircle = findViewById(R.id.femaleCircle)
        maleIcon = findViewById(R.id.maleIcon)
        femaleIcon = findViewById(R.id.femaleIcon)
        btnNext = findViewById(R.id.btnNextGender)
        skipText = findViewById(R.id.skipText)

        // Click listeners
        optionMale.setOnClickListener {
            selectedGender = "Male"
            setSelectedGender("male")
        }

        optionFemale.setOnClickListener {
            selectedGender = "Female"
            setSelectedGender("female")
        }

        btnNext.setOnClickListener {
            val intent = Intent(this, AssessmentGoalActivity::class.java)
            intent.putExtra("gender", selectedGender)
            startActivity(intent)
        }

        skipText.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun setSelectedGender(selected: String) {
        // Enable Next button
        btnNext.isEnabled = true
        btnNext.alpha = 1f

        val orange = ContextCompat.getColor(this, R.color.orange_500) // #F97316
        val white = ContextCompat.getColor(this, android.R.color.white)

        if (selected == "male") {
            maleCircle.setBackgroundResource(R.drawable.circle_orange)
            femaleCircle.setBackgroundResource(R.drawable.circle_white)
            maleIcon.setColorFilter(white)
            femaleIcon.setColorFilter(orange)
        } else {
            femaleCircle.setBackgroundResource(R.drawable.circle_orange)
            maleCircle.setBackgroundResource(R.drawable.circle_white)
            femaleIcon.setColorFilter(white)
            maleIcon.setColorFilter(orange)
        }
    }
}