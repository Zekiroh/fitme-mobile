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
        sharedViewModel.selectedGroupNames.observe(viewLifecycleOwner) { updatedList ->
            showCustomWorkout(rootView, updatedList)
        }
        return rootView
    }

    private fun showCustomWorkout(rootView: View, updatedList: MutableList<String>) {
        val workouts = WorkoutsTab()
        val addButton = rootView.findViewById<Button>(R.id.addWorkoutsId)
        val scrollView = ScrollView(requireContext())
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        addButton.visibility = if (updatedList.isEmpty()) View.VISIBLE else View.GONE
        addButton.setOnClickListener {
            tabSwitcher?.switchToWorkoutsTab()
        }

        workouts.workoutGroups
            .filterKeys { updatedList.contains(it)   }
            .forEach { groupName, exercises ->
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

                exercises.take(4).forEach { exercise ->
                    val exerciseName = TextView(requireContext()).apply {
                        text = "$exercise  4 x 8"
                        textSize = 14f
                        setTextColor(Color.DKGRAY)
                    }
                    groupLayout.addView(exerciseName)
                }

                val actionButtonsLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.END
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 16, 0, 0)
                    layoutParams = params
                }

                val viewAll = TextView(requireContext()).apply {
                    text = "View All"
                    textSize = 14f
                    setTextColor("#FF7F50".toColorInt())
                    val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    layoutParams = params
                    gravity = Gravity.END
                    setOnClickListener {
                        val fragment = WorkoutGroupDetailFragment.newInstance(groupName, ArrayList(exercises))
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.tabContentContainer, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }
                actionButtonsLayout.addView(viewAll)
                groupLayout.addView(actionButtonsLayout)
                layout.addView(groupLayout)
            }

        scrollView.addView(layout)

        val container = rootView.findViewById<LinearLayout>(R.id.customTabContainer)

        container.removeViews(1, container.childCount - 1)
        container.addView(scrollView)
    }


}
