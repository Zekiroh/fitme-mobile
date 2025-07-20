package com.samsantech.fitme.workouts

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.samsantech.fitme.R
import com.samsantech.fitme.model.Exercise
import com.samsantech.fitme.model.Nutrition
import com.samsantech.fitme.model.TrainingDay
import com.samsantech.fitme.model.WorkoutPlan
import androidx.core.view.isVisible
import com.samsantech.fitme.api.RetrofitClient
import com.samsantech.fitme.components.SharedPrefHelper
import com.samsantech.fitme.model.WorkoutPlanRequest
import com.samsantech.fitme.model.WorkoutPlanResponse
import com.samsantech.fitme.onboarding.AssessmentGenderActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlanTab : Fragment() {
private lateinit var containerLayout: LinearLayout
    private var sampleWorkoutPlan: WorkoutPlan? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_plan_tab, container, false)
        containerLayout = view.findViewById(R.id.containerLayout)
        val user = SharedPrefHelper.getLoggedInUser(requireContext())
        user?.let { userdata ->
            if(userdata.fitnessPlan == null || userdata.frequency == null) {
                getStartedButton()
            } else {
                RetrofitClient.members.getAiWorkoutSuggestion(WorkoutPlanRequest(
                    goal = userdata.fitnessPlan,
                    level = userdata.frequency
                )).enqueue(object: Callback<WorkoutPlanResponse> {
                    override fun onResponse(
                        call: Call<WorkoutPlanResponse?>,
                        response: Response<WorkoutPlanResponse?>
                    ) {
                        if(response.isSuccessful) {

                            val workoutPlan = response.body()?.data
                            sampleWorkoutPlan = workoutPlan as WorkoutPlan
                            populateWorkoutDays(workoutPlan.training_split)
                            populateNutritionSection(workoutPlan.nutrition)
                        }
                    }

                    override fun onFailure(
                        call: Call<WorkoutPlanResponse?>,
                        t: Throwable
                    ) {
                        println(t.message)
                    }

                })
            }
        }


        return view
    }

    private fun populateWorkoutDays(days: List<TrainingDay>) {
        val today = getTodayName() // "Monday", etc.

        for (day in days) {
            val view = layoutInflater.inflate(R.layout.item_workout_day, containerLayout, false)

            val dayTitle = view.findViewById<TextView>(R.id.dayTitle)
            val muscleGroup = view.findViewById<TextView>(R.id.muscleGroup)
            val startButton = view.findViewById<Button>(R.id.startButton)
            val exerciseContainer = view.findViewById<LinearLayout>(R.id.exerciseContainer)

            dayTitle.text = day.day
            muscleGroup.text = day.muscle_group

            val isToday = day.day.contains(today, ignoreCase = true)
            val isRest = day.exercises.isEmpty()

            when {
                isRest -> {
                    startButton.visibility = View.GONE
                    val restText = TextView(requireContext()).apply {
                        "Rest day. 🛌 Take a break and recover!".also { text = it }
                        setTextColor(Color.DKGRAY)
                        textSize = 15f
                        setPadding(0, 8, 0, 0)
                    }
                    exerciseContainer.visibility = View.VISIBLE
                    exerciseContainer.addView(restText)
                    containerLayout.addView(view)
                }

                isToday -> {
                    startButton.visibility = View.VISIBLE
                    exerciseContainer.visibility = View.VISIBLE

                    // Render exercises
                    for (exercise in day.exercises) {
                        val itemLayout = LinearLayout(requireContext()).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(16, 16, 16, 16)
                            background = ContextCompat.getDrawable(requireContext(), R.drawable.exercise_card_bg)
                            val params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            params.setMargins(0, 0, 0, 16)
                            layoutParams = params
                        }

                        val title = TextView(requireContext()).apply {
                            text = "• ${exercise.name}"
                            textSize = 16f
                            setTypeface(null, Typeface.BOLD)
                            setTextColor(Color.BLACK)
                        }

                        val setsLine = TextView(requireContext()).apply {
                            text = "Sets: ${exercise.sets}"
                            setTextColor(Color.parseColor("#555555"))
                            textSize = 14f
                            setPadding(0, 4, 0, 0)
                        }

                        val repsLine = TextView(requireContext()).apply {
                            text = "Reps per Set: ${exercise.reps.joinToString(" / ")}"
                            setTextColor(Color.parseColor("#555555"))
                            textSize = 14f
                            setPadding(0, 0, 0, 2)
                        }

                        val equipment = TextView(requireContext()).apply {
                            text = "Equipment: ${exercise.equipment} | Rest: ${exercise.rest_time_seconds}s"
                            textSize = 13f
                            setTextColor(Color.GRAY)
                        }

                        itemLayout.addView(title)
                        itemLayout.addView(setsLine)
                        itemLayout.addView(repsLine)
                        itemLayout.addView(equipment)

                        exerciseContainer.addView(itemLayout)
                    }

                    if (day.done) {
                        // ✅ Overlay with check icon on top of view (grayed)
                        startButton.isEnabled = false
                        startButton.alpha = 0.5f
                        view.alpha = 0.4f
                        view.isEnabled = false

                        val wrapper = FrameLayout(requireContext()).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                            )
                        }

                        // Detach from old parent if needed (safety)
                        (view.parent as? ViewGroup)?.removeView(view)

                        wrapper.addView(view)

                        val overlay = FrameLayout(requireContext()).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )
                            background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_card)
                        }

                        val checkIcon = ImageView(requireContext()).apply {
                            setImageResource(R.drawable.ic_success)
                            layoutParams = FrameLayout.LayoutParams(120, 120, Gravity.CENTER)
                            alpha = 0.9f
                        }

                        overlay.addView(checkIcon)
                        wrapper.addView(overlay)

                        containerLayout.addView(wrapper)
                    } else {
                        // Only add view directly once
                        containerLayout.addView(view)

                        startButton.setOnClickListener {
                            val intent = Intent(requireContext(), WorkoutSessionActivity::class.java)
                            intent.putExtra("exercises", ArrayList(day.exercises))
                            startActivity(intent)
                        }
                    }

                }


                else -> {
                    // 🔒 Locked: gray card with padlock icon
                    startButton.visibility = View.GONE
                    dayTitle.setTextColor(Color.LTGRAY)
                    muscleGroup.setTextColor(Color.LTGRAY)
                    exerciseContainer.visibility = View.VISIBLE
                    exerciseContainer.removeAllViews()

                    val lockWrapper = FrameLayout(requireContext()).apply {
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            200
                        )
                        params.setMargins(0, 16, 0, 0)
                        layoutParams = params
                        background = ContextCompat.getDrawable(requireContext(), R.drawable.locked_day_bg)
                    }

                    val lockIcon = ImageView(requireContext()).apply {
                        setImageResource(R.drawable.ic_lock)
                        layoutParams = FrameLayout.LayoutParams(96, 96, Gravity.CENTER)
                        alpha = 0.6f
                    }

                    lockWrapper.addView(lockIcon)
                    exerciseContainer.addView(lockWrapper)

                    containerLayout.addView(view)
                }
            }

        }
    }


    private fun getStartedButton() {
        if (sampleWorkoutPlan == null) {
            val button = Button(requireContext()).apply {
                text = "Get Started"
                textSize = 16f
                setPadding(24, 16, 24, 16)
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange)) // or your custom color
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                    topMargin = 48
                }

                setOnClickListener {
                    startActivity(Intent(requireContext(), AssessmentGenderActivity::class.java))
                }
            }

            containerLayout.addView(button)
        }
    }


    private fun populateNutritionSection(nutrition: Nutrition) {
        val card = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            background = resources.getDrawable(R.drawable.card_background, null)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 16, 0, 32)
            layoutParams = params
            elevation = 6f
        }

        val title = TextView(requireContext()).apply {
            text = "Nutrition Tips 🍽"
            textSize = 20f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 12)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val lines = listOf(
            "Caloric Surplus: ${nutrition.caloric_surplus}",
            "Protein: ${nutrition.protein}",
            "Carbohydrates: ${nutrition.carbohydrates}",
            "Fats: ${nutrition.fats}",
            "Meal Frequency: ${nutrition.meal_frequency}"
        )

        card.addView(title)

        for (line in lines) {
            val text = TextView(requireContext()).apply {
                text = "• $line"
                setTextColor(Color.DKGRAY)
                textSize = 16f
                setPadding(0, 4, 0, 4)
            }
            card.addView(text)
        }

        containerLayout.addView(card)
    }

    private fun getTodayName(): String {
        val calendar = java.util.Calendar.getInstance()
        return java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault()).format(calendar.time)
    }

}