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
import androidx.fragment.app.Fragment
import com.samsantech.fitme.R
import androidx.core.graphics.toColorInt

class WorkoutGroupDetailFragment : Fragment() {

    companion object {
        private const val ARG_GROUP_NAME = "group_name"
        private const val ARG_EXERCISES = "exercises"

        fun newInstance(groupName: String, exercises: ArrayList<String>): WorkoutGroupDetailFragment {
            val fragment = WorkoutGroupDetailFragment()
            val args = Bundle()
            args.putString(ARG_GROUP_NAME, groupName)
            args.putStringArrayList(ARG_EXERCISES, exercises)
            fragment.arguments = args
            return fragment
        }
    }

    private var groupName: String? = null
    private var exercises: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupName = it.getString(ARG_GROUP_NAME)
            exercises = it.getStringArrayList(ARG_EXERCISES)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val scrollView = ScrollView(requireContext())
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(requireContext()).apply {
            "$groupName • ${exercises?.size ?: 0} Exercises".also { text = it }
            textSize = 20f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }
        layout.addView(title)

        exercises?.forEach { exercise ->
            val exerciseLayout = LinearLayout(requireContext()).apply {
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

            val exerciseName = TextView(requireContext()).apply {
                "$exercise\n4 Sets x 8 Reps".also { text = it }
                textSize = 16f
                setTextColor(Color.DKGRAY)
            }

            exerciseLayout.addView(exerciseName)
            layout.addView(exerciseLayout)
        }

        val btnStart = Button(requireContext()).apply {
            "Start Workout".also { text = it }
            setBackgroundColor("#FF7F50".toColorInt())
            setTextColor(Color.WHITE)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 32, 0, 0)
            layoutParams = params
        }

        btnStart.setOnClickListener {
            val fragment = ActiveWorkoutFragment.newInstance(groupName ?: "Workout", exercises ?: arrayListOf())
            parentFragmentManager.beginTransaction()
                .replace(R.id.tabContentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }

        layout.addView(btnStart)
        scrollView.addView(layout)
        return scrollView
    }
}