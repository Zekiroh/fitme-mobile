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

class ListOfWorkoutsFragment : Fragment() {

    data class WorkoutGroup(val title: String, val exercises: List<String>)

    private val workoutGroups = listOf(
        WorkoutGroup(
            "Chest Workout",
            listOf("Bench Press - Barbell", "Incline Dumbbell Press", "Chest Fly - Dumbbell", "Push Up")
        ),
        WorkoutGroup(
            "Back Workout",
            listOf("Pull Up", "Lat Pulldown", "Seated Row - Cable", "Deadlift")
        ),
        WorkoutGroup(
            "Shoulder Workout",
            listOf("Overhead Press - Dumbbell", "Lateral Raise - Dumbbell", "Front Raise - Dumbbell", "Rear Delt Fly")
        ),
        WorkoutGroup(
            "Arm Workout",
            listOf("Bicep Curl - Dumbbell", "Triceps Pushdown - Cable", "Hammer Curl - Dumbbell", "Triceps Overhead Extension - Dumbbell")
        ),
        WorkoutGroup(
            "Abs Workout",
            listOf("Crunch", "Leg Raise", "Russian Twist", "Plank")
        ),
        WorkoutGroup(
            "Leg Workout",
            listOf("Squat - Barbell", "Lunges - Dumbbell", "Leg Press - Machine", "Leg Curl - Machine")
        ),
        WorkoutGroup(
            "Full Body Workout",
            listOf("Burpee", "Mountain Climber", "Kettlebell Swing", "Jump Squat")
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val scrollView = ScrollView(requireContext())
        val listLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        for (group in workoutGroups) {
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
                text = "${group.title}  • ${group.exercises.size} Exercises"
                textSize = 18f
                setTextColor(Color.BLACK)
            }

            val exercises = TextView(requireContext()).apply {
                text = group.exercises.joinToString("\n") { "$it   4 x 8" }
                textSize = 14f
                setTextColor(Color.DKGRAY)
            }

            val viewAll = TextView(requireContext()).apply {
                text = "View All"
                textSize = 14f
                setTextColor(Color.parseColor("#FF7F50"))
                gravity = Gravity.END
            }

            groupLayout.addView(title)
            groupLayout.addView(exercises)
            groupLayout.addView(viewAll)
            listLayout.addView(groupLayout)
        }

        scrollView.addView(listLayout)
        return scrollView
    }
}