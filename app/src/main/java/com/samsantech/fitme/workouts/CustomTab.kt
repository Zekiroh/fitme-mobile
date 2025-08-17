package com.samsantech.fitme.workouts

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.samsantech.fitme.R
import com.samsantech.fitme.workouts.CompletedWorkout
import java.text.SimpleDateFormat
import java.util.*

class CustomTab : Fragment() {
    private var tabSwitcher: WorkoutsTabSwitcher? = null
    private val sharedViewModel: SharedWorkoutViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabSwitcher = parentFragment as? WorkoutsTabSwitcher
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_custom_tab, container, false)
        
        val addButton = rootView.findViewById<Button>(R.id.addWorkoutsId)
        addButton.setOnClickListener {
            showExerciseSelectionModal()
        }

        // Observe selected exercises
        sharedViewModel.selectedExercises.observe(viewLifecycleOwner) { exercises ->
            showCustomWorkout(rootView, exercises)
        }

        // Observe workout completion status
        sharedViewModel.workoutCompletion.observe(viewLifecycleOwner) { completion ->
            if (completion.isCompleted) {
                // Refresh the view when workout is completed
                showCustomWorkout(rootView, sharedViewModel.selectedExercises.value ?: emptyList())
            }
        }

        // Observe completed workouts history
        sharedViewModel.completedWorkouts.observe(viewLifecycleOwner) { completedWorkouts ->
            // Refresh the view to show updated history
            showCustomWorkout(rootView, sharedViewModel.selectedExercises.value ?: emptyList())
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()
        // Refresh the view when returning from workout completion
        val rootView = view
        if (rootView != null) {
            val exercises = sharedViewModel.selectedExercises.value ?: emptyList()
            showCustomWorkout(rootView, exercises)
        }
    }

    private fun showExerciseSelectionModal() {
        val modal = ExerciseSelectionModal.newInstance { selectedExercises ->
            sharedViewModel.selectedExercises.value = selectedExercises.toMutableList()
        }
        modal.show(parentFragmentManager, "ExerciseSelectionModal")
    }

    private fun showCustomWorkout(rootView: View, exercises: List<CustomExercise>) {
        val addButton = rootView.findViewById<Button>(R.id.addWorkoutsId)
        val scrollView = ScrollView(requireContext())
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        // Check if there's a completed workout from ViewModel first, then from SharedPreferences
        val completionFromViewModel = sharedViewModel.workoutCompletion.value
        val sharedPrefs = requireActivity().getSharedPreferences("FitMePrefs", android.content.Context.MODE_PRIVATE)
        val isWorkoutCompleted = completionFromViewModel?.isCompleted == true || sharedPrefs.getBoolean("custom_workout_completed", false)
        
        val completionTime = completionFromViewModel?.completionTime ?: sharedPrefs.getLong("custom_workout_completion_time", 0)
        val lastWorkoutDetails = completionFromViewModel?.workoutDetails ?: (sharedPrefs.getString("last_custom_workout", "") ?: "")
        val lastWorkoutDuration = completionFromViewModel?.duration ?: sharedPrefs.getInt("last_custom_workout_duration", 0)

        if (isWorkoutCompleted && completionTime > 0) {
            // Show completion status and allow creating new workout
            showWorkoutCompletionStatus(layout, completionTime, lastWorkoutDetails, lastWorkoutDuration)
            
            // Clear the completed workout flags
            sharedPrefs.edit().putBoolean("custom_workout_completed", false).apply()
            sharedViewModel.clearWorkoutCompletion()
            
            // Clear selected exercises for new workout
            sharedViewModel.clearExercises()
            
            addButton.visibility = View.VISIBLE
        } else if (exercises.isEmpty()) {
            // No exercises selected yet
            addButton.visibility = View.VISIBLE
            
            val emptyMessage = TextView(requireContext()).apply {
                text = "No exercises selected yet.\nTap 'Add Workouts' to create your custom workout!"
                textSize = 16f
                setTextColor(Color.DKGRAY)
                gravity = Gravity.CENTER
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 64, 0, 0)
                layoutParams = params
            }
            layout.addView(emptyMessage)
        } else {
            // Show current workout plan
            addButton.visibility = View.GONE
            
            // Header
            val headerLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 0, 0, 24)
            }

            val title = TextView(requireContext()).apply {
                text = "Custom Workout • ${exercises.size} Exercises"
                textSize = 20f
                setTextColor(Color.BLACK)
                setTypeface(null, android.graphics.Typeface.BOLD)
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                layoutParams = params
            }

            val editButton = TextView(requireContext()).apply {
                text = "Edit"
                textSize = 16f
                setTextColor("#FF7F50".toColorInt())
                setOnClickListener {
                    showExerciseSelectionModal()
                }
            }

            headerLayout.addView(title)
            headerLayout.addView(editButton)
            layout.addView(headerLayout)

            // Exercise list with rest times
            exercises.forEachIndexed { index, exercise ->
                val exerciseCard = createExerciseCard(exercise, index == exercises.size - 1)
                layout.addView(exerciseCard)
            }

            // Start workout button
            val startButton = Button(requireContext()).apply {
                text = "Start Custom Workout"
                setBackgroundColor("#FF7F50".toColorInt())
                setTextColor(Color.WHITE)
                setOnClickListener {
                    val fragment = CustomWorkoutFragment.newInstance(exercises)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.tabContentContainer, fragment)
                        .addToBackStack(null)
                        .commit()
                }
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 32, 0, 0)
                layoutParams = params
            }
            layout.addView(startButton)
        }

        // Show workout history
        showWorkoutHistory(layout)

        scrollView.addView(layout)

        val container = rootView.findViewById<LinearLayout>(R.id.customTabContainer)
        container.removeViews(1, container.childCount - 1)
        container.addView(scrollView)
    }

    private fun showWorkoutHistory(layout: LinearLayout) {
        val completedWorkouts = sharedViewModel.getCompletedWorkouts()
        
        if (completedWorkouts.isNotEmpty()) {
            // History header
            val historyHeader = TextView(requireContext()).apply {
                text = "Workout History"
                textSize = 20f
                setTextColor(Color.BLACK)
                setTypeface(null, android.graphics.Typeface.BOLD)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 48, 0, 24)
                layoutParams = params
            }
            layout.addView(historyHeader)

            // Display completed workouts in reverse chronological order
            completedWorkouts.sortedByDescending { it.completionTime }.forEach { completedWorkout ->
                val historyCard = createHistoryCard(completedWorkout)
                layout.addView(historyCard)
            }

            // Clear history button
            val clearHistoryButton = Button(requireContext()).apply {
                text = "Clear History"
                setBackgroundColor(Color.LTGRAY)
                setTextColor(Color.DKGRAY)
                setOnClickListener {
                    sharedViewModel.clearWorkoutHistory()
                }
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 24, 0, 0)
                layoutParams = params
            }
            layout.addView(clearHistoryButton)
        }
    }

    private fun createHistoryCard(completedWorkout: CompletedWorkout): View {
        val cardLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(24, 24, 24, 24)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16)
            layoutParams = params
            elevation = 4f
        }

        // Completion time
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        val completionDate = Date(completedWorkout.completionTime)
        val timeText = TextView(requireContext()).apply {
            text = "Completed on ${dateFormat.format(completionDate)}"
            textSize = 14f
            setTextColor("#FF7F50".toColorInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        cardLayout.addView(timeText)

        // Workout details
        if (completedWorkout.workoutDetails.isNotEmpty()) {
            val exercises = completedWorkout.workoutDetails.split(",")
            exercises.forEach { exerciseDetail ->
                val exerciseText = TextView(requireContext()).apply {
                    text = "• $exerciseDetail"
                    textSize = 14f
                    setTextColor(Color.DKGRAY)
                    setPadding(0, 4, 0, 4)
                }
                cardLayout.addView(exerciseText)
            }
        }

        // Duration
        val durationText = TextView(requireContext()).apply {
            val minutes = completedWorkout.duration / 60
            val seconds = completedWorkout.duration % 60
            text = "Duration: ${minutes}m ${seconds}s"
            textSize = 14f
            setTextColor("#FF7F50".toColorInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 16, 0, 0)
        }
        cardLayout.addView(durationText)

        return cardLayout
    }

    private fun showWorkoutCompletionStatus(layout: LinearLayout, completionTime: Long, workoutDetails: String, duration: Int) {
        // Completion header
        val completionHeader = TextView(requireContext()).apply {
            text = "🎉 Workout Completed! 🎉"
            textSize = 24f
            setTextColor("#FF7F50".toColorInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 24)
            layoutParams = params
        }
        layout.addView(completionHeader)

        // Completion time
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        val completionDate = Date(completionTime)
        val timeText = TextView(requireContext()).apply {
            text = "Completed on ${dateFormat.format(completionDate)}"
            textSize = 16f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16)
            layoutParams = params
        }
        layout.addView(timeText)

        // Workout summary
        if (workoutDetails.isNotEmpty()) {
            val summaryCard = LinearLayout(requireContext()).apply {
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

            val summaryTitle = TextView(requireContext()).apply {
                text = "Workout Summary"
                textSize = 18f
                setTextColor(Color.BLACK)
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, 16)
            }
            summaryCard.addView(summaryTitle)

            val exercises = workoutDetails.split(",")
            exercises.forEach { exerciseDetail ->
                val exerciseText = TextView(requireContext()).apply {
                    text = "• $exerciseDetail"
                    textSize = 14f
                    setTextColor(Color.DKGRAY)
                    setPadding(0, 4, 0, 4)
                }
                summaryCard.addView(exerciseText)
            }

            val durationText = TextView(requireContext()).apply {
                val minutes = duration / 60
                val seconds = duration % 60
                text = "Duration: ${minutes}m ${seconds}s"
                textSize = 14f
                setTextColor("#FF7F50".toColorInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 16, 0, 0)
            }
            summaryCard.addView(durationText)

            layout.addView(summaryCard)
        }

        // Create new workout message
        val newWorkoutMessage = TextView(requireContext()).apply {
            text = "Ready for another workout? Tap 'Add Workouts' to create a new custom routine!"
            textSize = 16f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 32, 0, 0)
            layoutParams = params
        }
        layout.addView(newWorkoutMessage)
    }

    private fun createExerciseCard(exercise: CustomExercise, isLast: Boolean): View {
        val cardLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(24, 24, 24, 24)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, if (isLast) 0 else 16)
            layoutParams = params
            elevation = 8f
        }

        // Exercise name
        val exerciseName = TextView(requireContext()).apply {
            text = exercise.name
            textSize = 18f
            setTextColor(Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        cardLayout.addView(exerciseName)

        // Exercise details
        val detailsLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 16)
        }

        val setsText = TextView(requireContext()).apply {
            text = "${exercise.sets} Sets"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            params.setMargins(0, 0, 16, 0)
            layoutParams = params
        }

        val repsText = TextView(requireContext()).apply {
            text = "${exercise.reps} Reps"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            params.setMargins(0, 0, 16, 0)
            layoutParams = params
        }

        val restText = TextView(requireContext()).apply {
            text = "${exercise.restTimeSeconds}s Rest"
            textSize = 14f
            setTextColor("#FF7F50".toColorInt())
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            layoutParams = params
        }

        detailsLayout.addView(setsText)
        detailsLayout.addView(repsText)
        detailsLayout.addView(restText)
        cardLayout.addView(detailsLayout)

        // Rest time indicator (if not last exercise)
        if (!isLast) {
            val restIndicator = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                setPadding(0, 16, 0, 0)
            }

            val restIcon = TextView(requireContext()).apply {
                text = "⏱"
                textSize = 20f
                setTextColor("#FF7F50".toColorInt())
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 8, 0)
                layoutParams = params
            }

            val restLabel = TextView(requireContext()).apply {
                text = "Rest for ${exercise.restTimeSeconds} seconds"
                textSize = 14f
                setTextColor("#FF7F50".toColorInt())
                setTypeface(null, android.graphics.Typeface.ITALIC)
            }

            restIndicator.addView(restIcon)
            restIndicator.addView(restLabel)
            cardLayout.addView(restIndicator)
        }

        return cardLayout
    }
}
