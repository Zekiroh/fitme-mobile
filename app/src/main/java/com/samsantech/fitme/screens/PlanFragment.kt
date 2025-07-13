package com.samsantech.fitme.screens

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.samsantech.fitme.R

class PlanFragment : Fragment() {

    private val plan = mutableListOf<DayItem>()
    private val PREFS_NAME = "WorkoutPrefs"
    private val KEY_COMPLETED_DAYS = "CompletedDays"

    data class DayItem(val dayNumber: Int, val isRest: Boolean, var isCompleted: Boolean)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_plan, container, false)
        setupPlan(view)
        return view
    }

    private fun setupPlan(view: View) {
        val sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val completedSet = sharedPrefs.getStringSet(KEY_COMPLETED_DAYS, emptySet()) ?: emptySet()

        plan.clear()
        plan.addAll(
            listOf(
                DayItem(1, false, completedSet.contains("1")),
                DayItem(2, false, completedSet.contains("2")),
                DayItem(3, false, completedSet.contains("3")),
                DayItem(4, true, completedSet.contains("4")),
                DayItem(5, false, completedSet.contains("5"))
            )
        )

        val scrollView = view.findViewById<ScrollView>(R.id.planScrollView)
        val dayListLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        for (i in plan.indices) {
            val day = plan[i]
            val isButtonEnabled = i == 0 || plan[i - 1].isCompleted || plan[i - 1].isRest

            val itemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(Color.WHITE)
                setPadding(24, 24, 24, 24)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 24)
                layoutParams = params
                elevation = 8f
            }

            val dayLabel = TextView(requireContext()).apply {
                text = if (day.isRest) "Rest Day" else "Day ${day.dayNumber}"
                textSize = 16f
                setTextColor(Color.BLACK)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val detailLabel = TextView(requireContext()).apply {
                text = if (day.isRest) "Take a break and recover!" else when (day.dayNumber) {
                    1 -> "Chest"
                    2 -> "Back"
                    3 -> "Lower body"
                    5 -> "Shoulders"
                    else -> ""
                }
                textSize = 14f
                setTextColor(Color.DKGRAY)
            }

            val actionView: View = when {
                day.isRest -> {
                    TextView(requireContext()).apply {
                        text = "🛌"
                        textSize = 18f
                    }
                }
                isButtonEnabled -> {
                    Button(requireContext()).apply {
                        text = "Start"
                        setBackgroundColor(Color.parseColor("#FF7F50"))
                        setTextColor(Color.WHITE)
                        setOnClickListener {
                            day.isCompleted = true
                            val updatedCompletedSet = plan.filter { it.isCompleted }
                                .map { it.dayNumber.toString() }.toSet()
                            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                                .putStringSet(KEY_COMPLETED_DAYS, updatedCompletedSet)
                                .apply()
                            setupPlan(view)
                            Toast.makeText(context, "Started Day ${day.dayNumber}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else -> {
                    TextView(requireContext()).apply {
                        text = "🔒"
                        textSize = 18f
                        gravity = Gravity.CENTER
                    }
                }
            }

            val dayInfoLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                addView(dayLabel)
                addView(detailLabel)
            }

            itemLayout.addView(dayInfoLayout)
            itemLayout.addView(actionView)
            dayListLayout.addView(itemLayout)
        }

        scrollView.removeAllViews()
        scrollView.addView(dayListLayout)
    }
}