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

        return rootView
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

        addButton.visibility = if (exercises.isEmpty()) View.VISIBLE else View.GONE

        if (exercises.isEmpty()) {
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

        scrollView.addView(layout)

        val container = rootView.findViewById<LinearLayout>(R.id.customTabContainer)
        container.removeViews(1, container.childCount - 1)
        container.addView(scrollView)
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
