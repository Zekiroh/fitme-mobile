package com.samsantech.fitme.workouts

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.samsantech.fitme.R

class WorkoutsTab : Fragment() {

    private val workoutGroups = mapOf(
        "Chest Workout" to listOf("Bench Press - Barbell", "Incline Dumbbell Press", "Chest Fly - Dumbbell", "Push Up"),
        "Back Workout" to listOf("Pull Up", "Lat Pulldown", "Seated Row - Cable", "Deadlift"),
        "Shoulder Workout" to listOf("Overhead Press - Dumbbell", "Lateral Raise - Dumbbell", "Front Raise - Dumbbell", "Rear Delt Fly"),
        "Arm Workout" to listOf("Bicep Curl - Dumbbell", "Triceps Pushdown - Cable", "Hammer Curl - Dumbbell", "Triceps Overhead Extension - Dumbbell"),
        "Abs Workout" to listOf("Crunch", "Leg Raise", "Russian Twist", "Plank"),
        "Leg Workout" to listOf("Squat - Barbell", "Lunges - Dumbbell", "Leg Press - Machine", "Leg Curl - Machine"),
        "Full Body Workout" to listOf("Burpee", "Mountain Climber", "Kettlebell Swing", "Jump Squat")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val scrollView = ScrollView(requireContext())
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        workoutGroups.forEach { (groupName, exercises) ->
            val groupLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
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

            val title = TextView(requireContext()).apply {
                text = "$groupName • ${exercises.size} Exercises"
                textSize = 18f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
            }
            groupLayout.addView(title)

            exercises.take(3).forEach { exercise ->
                val exerciseName = TextView(requireContext()).apply {
                    text = "$exercise  4 x 8"
                    textSize = 14f
                    setTextColor(Color.DKGRAY)
                }
                groupLayout.addView(exerciseName)
            }

            val viewAll = TextView(requireContext()).apply {
                text = "View All"
                textSize = 14f
                setTextColor(Color.parseColor("#FF7F50"))
                gravity = Gravity.END
                setOnClickListener {
                    val fragment = WorkoutGroupDetailFragment.newInstance(groupName, ArrayList(exercises))
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.tabContentContainer, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }

            groupLayout.addView(viewAll)
            layout.addView(groupLayout)
        }

        scrollView.addView(layout)
        return scrollView
    }
}