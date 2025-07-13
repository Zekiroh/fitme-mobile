package com.samsantech.fitme.workouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.samsantech.fitme.R

class ExerciseDetailFragment : Fragment() {

    companion object {
        private const val ARG_EXERCISE_NAME = "exercise_name"

        fun newInstance(exerciseName: String): ExerciseDetailFragment {
            val fragment = ExerciseDetailFragment()
            val args = Bundle()
            args.putString(ARG_EXERCISE_NAME, exerciseName)
            fragment.arguments = args
            return fragment
        }
    }

    private var exerciseName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            exerciseName = it.getString(ARG_EXERCISE_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercise_detail, container, false)

        val tvExerciseName: TextView = view.findViewById(R.id.tvExerciseName)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val ivImage: ImageView = view.findViewById(R.id.ivExerciseImage)

        tvExerciseName.text = exerciseName ?: "Exercise Name"
        tvDescription.text = "This exercise helps strengthen and build muscle. Perform 4 sets of 8 reps. Focus on proper form and control each rep for maximum results."

        // Placeholder image — replace with actual exercise images later
        ivImage.setImageResource(R.drawable.ic_exercise)

        return view
    }
}