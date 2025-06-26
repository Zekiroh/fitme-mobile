package com.samsantech.fitme.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.samsantech.fitme.R

class BMIInfoCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val bmiValueText: TextView
    private val bmiCategoryText: TextView
    private val bmiTipText: TextView
    private val infoIcon: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.bmi_info_card, this, true)
        orientation = VERTICAL
        bmiValueText = findViewById(R.id.bmiValueText)
        bmiCategoryText = findViewById(R.id.bmiCategoryText)
        bmiTipText = findViewById(R.id.bmiTipText)
        infoIcon = findViewById(R.id.bmiInfoIcon)

        infoIcon.setOnClickListener {
            showBottomSheetInfo()
        }
    }

    private fun showBottomSheetInfo() {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.bmi_info_sheet, null)
        dialog.setContentView(view)
        dialog.show()
    }

    fun updateBMI(weight: Int, height: Int, unit: String) {
        val heightInMeters = if (unit == "cm") height / 100.0 else (height * 2.54) / 100.0
        val bmi = weight / (heightInMeters * heightInMeters)
        val category: String
        val tip: String

        val color = when {
            bmi < 18.5 -> {
                category = "Underweight"
                tip = "Consider adding more nutritious calories and strength training."
                R.color.bmi_underweight
            }
            bmi < 24.9 -> {
                category = "Normal"
                tip = "You're doing great! Keep up a healthy lifestyle."
                R.color.bmi_normal
            }
            bmi < 29.9 -> {
                category = "Overweight"
                tip = "Consider increasing physical activity and watching your diet."
                R.color.bmi_overweight
            }
            else -> {
                category = "Obese"
                tip = "Talk to a healthcare provider for a personalized wellness plan."
                R.color.bmi_obese
            }
        }

        bmiValueText.text = String.format("Your Current BMI: %.1f", bmi)
        bmiCategoryText.text = category
        bmiCategoryText.setTextColor(context.getColor(color))
        bmiTipText.text = tip
    }
}
