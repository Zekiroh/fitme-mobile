package com.samsantech.fitme.screens

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.samsantech.fitme.R
import com.samsantech.fitme.api.RetrofitClient
import com.samsantech.fitme.model.Workout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.graphics.Color

class RecommendedWorkoutsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recommended_workouts, container, false)
        fetchWorkouts(view)
        return view
    }

    private fun fetchWorkouts(view: View) {
        val container = view.findViewById<FrameLayout>(R.id.recommendedContainer)

        val loading = ProgressBar(requireContext())
        container.addView(loading)

        val goal = "Build Muscle" // Use your real dynamic value
        val level = "Intermediate" // Use your real dynamic value

        RetrofitClient.recommendation.getRecommendations(goal, level)
            .enqueue(object : Callback<List<Workout>> {
                override fun onResponse(call: Call<List<Workout>>, response: Response<List<Workout>>) {
                    container.removeAllViews()
                    if (response.isSuccessful) {
                        val workouts = response.body() ?: emptyList()
                        val scrollView = ScrollView(requireContext())
                        val listLayout = LinearLayout(requireContext()).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(32, 32, 32, 32)
                        }

                        for (workout in workouts) {
                            val itemLayout = LinearLayout(requireContext()).apply {
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

                            val nameText = TextView(requireContext()).apply {
                                text = workout.name
                                textSize = 18f
                                setTextColor(Color.BLACK)
                            }

                            val detailText = TextView(requireContext()).apply {
                                text = "Category: ${workout.category}\nMuscle: ${workout.muscle_group}\nDuration: ${workout.duration} mins"
                                textSize = 14f
                                setTextColor(Color.DKGRAY)
                            }

                            itemLayout.addView(nameText)
                            itemLayout.addView(detailText)
                            listLayout.addView(itemLayout)
                        }

                        scrollView.addView(listLayout)
                        container.addView(scrollView)
                    } else {
                        val errorText = TextView(requireContext()).apply {
                            text = "Failed to load workouts: ${response.code()}"
                            setTextColor(Color.RED)
                            textSize = 16f
                            gravity = Gravity.CENTER
                        }
                        container.addView(errorText)
                    }
                }

                override fun onFailure(call: Call<List<Workout>>, t: Throwable) {
                    container.removeAllViews()
                    val errorText = TextView(requireContext()).apply {
                        text = "Error: ${t.message}"
                        setTextColor(Color.RED)
                        textSize = 16f
                        gravity = Gravity.CENTER
                    }
                    container.addView(errorText)
                }
            })
    }
}