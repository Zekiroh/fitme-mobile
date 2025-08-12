package com.samsantech.fitme.workouts

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.samsantech.fitme.R

class ExerciseSelectionModal : DialogFragment() {
    private val sharedViewModel: SharedWorkoutViewModel by activityViewModels()
    private var onExercisesSelected: ((List<CustomExercise>) -> Unit)? = null

    companion object {
        fun newInstance(onExercisesSelected: (List<CustomExercise>) -> Unit): ExerciseSelectionModal {
            val fragment = ExerciseSelectionModal()
            fragment.onExercisesSelected = onExercisesSelected
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        
        val window = dialog.window
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }

        // Header
        val headerLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor("#FF7F50".toColorInt())
            setPadding(24, 24, 24, 24)
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(requireContext()).apply {
            text = "Select Exercises"
            textSize = 20f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val closeButton = TextView(requireContext()).apply {
            text = "✕"
            textSize = 24f
            setTextColor(Color.WHITE)
            setOnClickListener { dismiss() }
        }

        headerLayout.addView(title, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        headerLayout.addView(closeButton)
        rootView.addView(headerLayout)

        // Exercise list
        val scrollView = ScrollView(requireContext())
        val exerciseLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val allExercises = getAllExercises()
        val selectedExercises = mutableListOf<CustomExercise>()

        allExercises.forEach { exercise ->
            val exerciseCard = createExerciseCard(exercise, selectedExercises)
            exerciseLayout.addView(exerciseCard)
        }

        scrollView.addView(exerciseLayout)
        rootView.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ))

        // Bottom buttons
        val bottomLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.WHITE)
            elevation = 8f
        }

        val cancelButton = Button(requireContext()).apply {
            text = "Cancel"
            setBackgroundColor(Color.GRAY)
            setTextColor(Color.WHITE)
            setOnClickListener { dismiss() }
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            params.setMargins(0, 0, 16, 0)
            layoutParams = params
        }

        val confirmButton = Button(requireContext()).apply {
            text = "Create Workout"
            setBackgroundColor("#FF7F50".toColorInt())
            setTextColor(Color.WHITE)
            setOnClickListener {
                if (selectedExercises.isNotEmpty()) {
                    onExercisesSelected?.invoke(selectedExercises)
                    dismiss()
                }
            }
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            params.setMargins(16, 0, 0, 0)
            layoutParams = params
        }

        bottomLayout.addView(cancelButton)
        bottomLayout.addView(confirmButton)
        rootView.addView(bottomLayout)

        return rootView
    }

    private fun getAllExercises(): List<String> {
        return listOf(
            "Bench Press - Barbell",
            "Incline Dumbbell Press",
            "Chest Fly - Dumbbell",
            "Push Up",
            "Pull Up",
            "Lat Pulldown",
            "Seated Row - Cable",
            "Deadlift",
            "Overhead Press - Dumbbell",
            "Lateral Raise - Dumbbell",
            "Front Raise - Dumbbell",
            "Rear Delt Fly",
            "Bicep Curl - Dumbbell",
            "Triceps Pushdown - Cable",
            "Hammer Curl - Dumbbell",
            "Triceps Overhead Extension - Dumbbell",
            "Crunch",
            "Leg Raise",
            "Russian Twist",
            "Plank",
            "Squat - Barbell",
            "Lunges - Dumbbell",
            "Leg Press - Machine",
            "Leg Curl - Machine",
            "Burpee",
            "Mountain Climber",
            "Kettlebell Swing",
            "Jump Squat"
        )
    }

    private fun createExerciseCard(exerciseName: String, selectedExercises: MutableList<CustomExercise>): View {
        val cardLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(16, 16, 16, 16)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16)
            layoutParams = params
            elevation = 4f
        }

        // Exercise name and checkbox
        val headerLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val exerciseNameText = TextView(requireContext()).apply {
            text = exerciseName
            textSize = 16f
            setTextColor(Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            layoutParams = params
        }

        val checkbox = TextView(requireContext()).apply {
            text = "☐"
            textSize = 24f
            setTextColor("#FF7F50".toColorInt())
            setOnClickListener {
                val isSelected = selectedExercises.any { it.name == exerciseName }
                if (isSelected) {
                    selectedExercises.removeAll { it.name == exerciseName }
                    text = "☐"
                } else {
                    selectedExercises.add(CustomExercise(exerciseName))
                    text = "☑"
                }
            }
        }

        headerLayout.addView(exerciseNameText)
        headerLayout.addView(checkbox)
        cardLayout.addView(headerLayout)

        // Exercise settings (initially hidden)
        val settingsLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(0, 16, 0, 0)
        }

        // Sets input
        val setsLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }

        val setsLabel = TextView(requireContext()).apply {
            text = "Sets:"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 16, 0)
            layoutParams = params
        }

        val setsInput = EditText(requireContext()).apply {
            hint = "3"
            textSize = 14f
            setText("3")
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams = params
        }

        setsLayout.addView(setsLabel)
        setsLayout.addView(setsInput)

        // Reps input
        val repsLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }

        val repsLabel = TextView(requireContext()).apply {
            text = "Reps:"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 16, 0)
            layoutParams = params
        }

        val repsInput = EditText(requireContext()).apply {
            hint = "10"
            textSize = 14f
            setText("10")
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams = params
        }

        repsLayout.addView(repsLabel)
        repsLayout.addView(repsInput)

        // Rest time input
        val restLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }

        val restLabel = TextView(requireContext()).apply {
            text = "Rest (sec):"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 16, 0)
            layoutParams = params
        }

        val restInput = EditText(requireContext()).apply {
            hint = "60"
            textSize = 14f
            setText("60")
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams = params
        }

        restLayout.addView(restLabel)
        restLayout.addView(restInput)

        settingsLayout.addView(setsLayout)
        settingsLayout.addView(repsLayout)
        settingsLayout.addView(restLayout)

        cardLayout.addView(settingsLayout)

        // Show/hide settings when exercise is selected
        checkbox.setOnClickListener {
            val isSelected = selectedExercises.any { it.name == exerciseName }
            if (isSelected) {
                selectedExercises.removeAll { it.name == exerciseName }
                checkbox.text = "☐"
                settingsLayout.visibility = View.GONE
            } else {
                selectedExercises.add(CustomExercise(
                    name = exerciseName,
                    sets = setsInput.text.toString().toIntOrNull() ?: 3,
                    reps = repsInput.text.toString().toIntOrNull() ?: 10,
                    restTimeSeconds = restInput.text.toString().toIntOrNull() ?: 60
                ))
                checkbox.text = "☑"
                settingsLayout.visibility = View.VISIBLE
            }
        }

        // Update exercise when settings change
        setsInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val existingExercise = selectedExercises.find { it.name == exerciseName }
                if (existingExercise != null) {
                    val newSets = s.toString().toIntOrNull() ?: 3
                    val index = selectedExercises.indexOf(existingExercise)
                    selectedExercises[index] = existingExercise.copy(sets = newSets)
                }
            }
        })

        repsInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val existingExercise = selectedExercises.find { it.name == exerciseName }
                if (existingExercise != null) {
                    val newReps = s.toString().toIntOrNull() ?: 10
                    val index = selectedExercises.indexOf(existingExercise)
                    selectedExercises[index] = existingExercise.copy(reps = newReps)
                }
            }
        })

        restInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val existingExercise = selectedExercises.find { it.name == exerciseName }
                if (existingExercise != null) {
                    val newRest = s.toString().toIntOrNull() ?: 60
                    val index = selectedExercises.indexOf(existingExercise)
                    selectedExercises[index] = existingExercise.copy(restTimeSeconds = newRest)
                }
            }
        })

        return cardLayout
    }
}
