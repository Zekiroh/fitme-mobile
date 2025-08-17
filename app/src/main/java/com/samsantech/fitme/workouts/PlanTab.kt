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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.samsantech.fitme.R
import com.samsantech.fitme.model.Exercise
import com.samsantech.fitme.model.Nutrition
import com.samsantech.fitme.model.TrainingDay
import com.samsantech.fitme.model.WorkoutPlan
import com.samsantech.fitme.model.User
import androidx.core.view.isVisible
import com.samsantech.fitme.api.RetrofitClient
import com.samsantech.fitme.components.SharedPrefHelper
import com.samsantech.fitme.model.WorkoutPlanRequest
import com.samsantech.fitme.model.WorkoutPlanResponse
import com.samsantech.fitme.onboarding.AssessmentGenderActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Extension function to capitalize first letter
private fun String.capitalize(): String {
    return if (isNotEmpty()) this[0].uppercase() + substring(1).lowercase() else this
}

class PlanTab : Fragment() {
    private lateinit var containerLayout: LinearLayout
    private var sampleWorkoutPlan: WorkoutPlan? = null
    private lateinit var loadingView: LinearLayout
    private lateinit var errorView: LinearLayout
    private var loadingTimeoutHandler: android.os.Handler? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_plan_tab, container, false)
        containerLayout = view.findViewById(R.id.containerLayout)
        
        // Initialize loading and error views
        setupLoadingView()
        setupErrorView()
        
        val user = SharedPrefHelper.getLoggedInUser(requireContext())
        user?.let { userdata ->
            if(userdata.fitnessPlan.isNullOrBlank() || userdata.frequency.isNullOrBlank()) {
                hideLoading()
                showWelcomeMessage()
                getStartedButton()
            } else {
                showLoading()
                fetchWorkoutPlan(userdata)
            }
        } ?: run {
            hideLoading()
            showError("User not found. Please login again.")
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Refresh workout completion status when returning from workout session
        sampleWorkoutPlan?.training_split?.let { days ->
            refreshAllWorkoutDays(days)
            // Show updated completion summary
            showWorkoutCompletionSummary()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up timeout handler to prevent memory leaks
        loadingTimeoutHandler?.removeCallbacksAndMessages(null)
        loadingTimeoutHandler = null
    }

    private fun setupLoadingView() {
        loadingView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 64, 32, 64)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            
            val progressBar = ProgressBar(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                indeterminateTintList = ContextCompat.getColorStateList(requireContext(), R.color.orange)
            }
            
            val loadingText = TextView(requireContext()).apply {
                text = "AI is crafting your perfect workout plan... 🧠💪"
                textSize = 16f
                setTextColor(Color.DKGRAY)
                setPadding(0, 16, 0, 0)
                gravity = Gravity.CENTER
            }
            
            val loadingSubtext = TextView(requireContext()).apply {
                text = "This may take a few moments"
                textSize = 12f
                setTextColor(Color.GRAY)
                setPadding(0, 8, 0, 0)
                gravity = Gravity.CENTER
            }
            
            addView(progressBar)
            addView(loadingText)
            addView(loadingSubtext)
        }
    }

    private fun setupErrorView() {
        errorView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 64, 32, 64)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            
            val errorIcon = TextView(requireContext()).apply {
                text = "⚠️"
                textSize = 48f
                gravity = Gravity.CENTER
            }
            
            val errorTitle = TextView(requireContext()).apply {
                text = "Oops! Something went wrong"
                textSize = 18f
                setTextColor(Color.BLACK)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 16, 0, 8)
                gravity = Gravity.CENTER
            }
            
            val errorMessage = TextView(requireContext()).apply {
                text = "We couldn't load your workout plan. Please try again."
                textSize = 14f
                setTextColor(Color.DKGRAY)
                setPadding(0, 0, 0, 16)
                gravity = Gravity.CENTER
            }
            
            val retryButton = Button(requireContext()).apply {
                text = "Retry"
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange))
                setTextColor(Color.WHITE)
                setPadding(24, 12, 24, 12)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                
                setOnClickListener {
                    hideError()
                    val user = SharedPrefHelper.getLoggedInUser(requireContext())
                    user?.let { userdata ->
                        if(userdata.fitnessPlan.isNullOrBlank() || userdata.frequency.isNullOrBlank()) {
                            showError("Please complete your fitness assessment first.")
                        } else {
                            showLoading()
                            fetchWorkoutPlan(userdata)
                        }
                    } ?: run {
                        showError("User not found. Please login again.")
                    }
                }
            }
            
            addView(errorIcon)
            addView(errorTitle)
            addView(errorMessage)
            addView(retryButton)
        }
    }

    private fun showLoading() {
        containerLayout.removeAllViews()
        containerLayout.addView(loadingView)
        
        // Set a timeout for loading (30 seconds)
        loadingTimeoutHandler?.removeCallbacksAndMessages(null)
        loadingTimeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
        loadingTimeoutHandler?.postDelayed({
            if (loadingView.parent != null) {
                hideLoading()
                showError("Request timed out. Please check your internet connection and try again.")
            }
        }, 30000) // 30 seconds timeout
    }

    private fun hideLoading() {
        // Clear timeout handler
        loadingTimeoutHandler?.removeCallbacksAndMessages(null)
        
        if (loadingView.parent != null) {
            (loadingView.parent as ViewGroup).removeView(loadingView)
        }
    }

    private fun showError(message: String) {
        containerLayout.removeAllViews()
        // Update error message if needed
        val errorMessageView = errorView.getChildAt(2) as TextView
        errorMessageView.text = message
        containerLayout.addView(errorView)
    }

    private fun hideError() {   
        if (errorView.parent != null) {
            (errorView.parent as ViewGroup).removeView(errorView)
        }
    }

    private fun showWelcomeMessage() {
        val welcomeView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            background = ContextCompat.getDrawable(requireContext(), R.drawable.card_background)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 16)
            }
            elevation = 6f
        }
        
        val welcomeIcon = TextView(requireContext()).apply {
            text = "🎯"
            textSize = 48f
            gravity = Gravity.CENTER
        }
        
        val welcomeTitle = TextView(requireContext()).apply {
            text = "Welcome to FitMe!"
            textSize = 20f
            setTextColor(Color.BLACK)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 16, 0, 8)
            gravity = Gravity.CENTER
        }
        
        val welcomeMessage = TextView(requireContext()).apply {
            text = "Let's create your personalized fitness journey. Complete a quick assessment to get started with your custom workout plan."
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, 0, 16)
            gravity = Gravity.CENTER
        }
        
        welcomeView.addView(welcomeIcon)
        welcomeView.addView(welcomeTitle)
        welcomeView.addView(welcomeMessage)
        
        // Add reset button for testing
        val resetButton = Button(requireContext()).apply {
            text = "Reset Progress"
            textSize = 12f
            setBackgroundColor(Color.LTGRAY)
            setTextColor(Color.DKGRAY)
            setPadding(16, 8, 16, 8)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
                topMargin = 16
            }
            
            setOnClickListener {
                resetAllWorkoutProgress()
                Toast.makeText(requireContext(), "Progress reset!", Toast.LENGTH_SHORT).show()
            }
        }
        
        welcomeView.addView(resetButton)
        containerLayout.addView(welcomeView)
    }

    private fun showSuccessMessage(message: String) {
        val successView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(16, 12, 16, 12)
            background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_card)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 16)
            }
            elevation = 8f
        }
        
        val successIcon = TextView(requireContext()).apply {
            text = "✅"
            textSize = 20f
            setPadding(0, 0, 8, 0)
        }
        
        val successText = TextView(requireContext()).apply {
            text = message
            textSize = 14f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER_VERTICAL
        }
        
        successView.addView(successIcon)
        successView.addView(successText)
        
        // Add success message at the top
        containerLayout.addView(successView, 0)
        
        // Remove success message after 3 seconds
        successView.postDelayed({
            if (successView.parent != null) {
                (successView.parent as ViewGroup).removeView(successView)
            }
        }, 3000)
    }

    private fun showWorkoutCompletionSummary() {
        val completedDays = sampleWorkoutPlan?.training_split?.count { day ->
            isWorkoutDayCompleted(day)
        } ?: 0
        
        val totalDays = sampleWorkoutPlan?.training_split?.size ?: 0
        
        if (completedDays > 0) {
            val summaryView = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                setPadding(16, 12, 16, 12)
                background = ContextCompat.getDrawable(requireContext(), R.drawable.card_background)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 16, 16, 16)
                }
                elevation = 6f
            }
            
            val summaryIcon = TextView(requireContext()).apply {
                text = "🏆"
                textSize = 20f
                setPadding(0, 0, 8, 0)
            }
            
            val summaryText = TextView(requireContext()).apply {
                text = "Progress: $completedDays/$totalDays workout days completed!"
                textSize = 14f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER_VERTICAL
                setTypeface(null, Typeface.BOLD)
            }
            
            summaryView.addView(summaryIcon)
            summaryView.addView(summaryText)
            
            // Add summary at the top after success message
            if (containerLayout.childCount > 0) {
                containerLayout.addView(summaryView, 1)
            } else {
                containerLayout.addView(summaryView, 0)
            }
        }
    }

    private fun fetchWorkoutPlan(userdata: User) {
        // Update loading message with user's specific details
        val loadingText = loadingView.getChildAt(1) as TextView
        val loadingSubtext = loadingView.getChildAt(2) as TextView
        
        loadingText.text = "AI is crafting your ${userdata.fitnessPlan?.lowercase() ?: "fitness"} plan... 🧠💪"
        loadingSubtext.text = "Level: ${userdata.frequency?.capitalize() ?: "Beginner"} • This may take a few moments"
        
        RetrofitClient.aiFitness.getAiWorkoutFitness(WorkoutPlanRequest(
            user_id = userdata.id,
            goal = userdata.fitnessPlan,
            level = userdata.frequency
        )).enqueue(object: Callback<WorkoutPlanResponse> {
            override fun onResponse(
                call: Call<WorkoutPlanResponse?>,
                response: Response<WorkoutPlanResponse?>
            ) {
                hideLoading()
                
                if(response.isSuccessful) {
                    val workoutPlan = response.body()?.plan
                    print(workoutPlan)
                    // Add proper null checking
                    if (workoutPlan != null) {
                        try {
                            sampleWorkoutPlan = workoutPlan
                            populateWorkoutDays(workoutPlan.training_split)
                            populateNutritionSection(workoutPlan.nutrition)
                            
                            // Show success notification
                            showSuccessMessage("Workout plan loaded successfully! 🎉")
                            // Show completion summary
                            showWorkoutCompletionSummary()
                        } catch (e: Exception) {
                            showError("Failed to process workout plan: ${e.message}")
                        }
                    } else {
                        showError("The server didn't return any workout plan data. Please try again or contact support.")
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        404 -> "Workout plan not found for your profile"
                        500 -> "Server error. Please try again later"
                        else -> "Failed to load workout plan (${response.code()})"
                    }
                    showError(errorMessage)
                }
            }

            override fun onFailure(
                call: Call<WorkoutPlanResponse?>,
                t: Throwable
            ) {
                hideLoading()
                val errorMessage = when {
                    t.message?.contains("timeout", ignoreCase = true) == true -> 
                        "Request timed out. Please check your internet connection."
                    t.message?.contains("network", ignoreCase = true) == true -> 
                        "Network error. Please check your internet connection."
                    else -> "Failed to connect: ${t.message ?: "Unknown error"}"
                }
                showError(errorMessage)
            }
        })
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
            val isCompleted = day.done || isWorkoutDayCompleted(day)

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

                    // Add progress indicator for workout days
                    val completedExercises = day.exercises.count { exercise ->
                        isExerciseCompleted(day.day, exercise.name)
                    }
                    val totalExercises = day.exercises.size
                    
                    if (totalExercises > 0) {
                        val progressText = TextView(requireContext()).apply {
                            text = "Progress: $completedExercises/$totalExercises exercises"
                            textSize = 12f
                            setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
                            setTypeface(null, Typeface.BOLD)
                            setPadding(0, 4, 0, 0)
                        }
                        exerciseContainer.addView(progressText)
                    }

                    // Render exercises with completion status
                    for (exercise in day.exercises) {
                        val isExerciseCompleted = isExerciseCompleted(day.day, exercise.name)
                        val itemLayout = createExerciseLayout(exercise, isExerciseCompleted, day.day)
                        exerciseContainer.addView(itemLayout)
                    }

                    if (isCompleted) {
                        // ✅ Mark as completed with enhanced visual feedback
                        markWorkoutDayAsCompleted(view, startButton, dayTitle, muscleGroup)
                    } else {
                        // Only add view directly once
                        containerLayout.addView(view)

                        startButton.setOnClickListener {
                            val intent = Intent(requireContext(), WorkoutSessionActivity::class.java)
                            intent.putExtra("exercises", ArrayList(day.exercises))
                            intent.putExtra("dayName", day.day)
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

    private fun createExerciseLayout(exercise: Exercise, isCompleted: Boolean, dayName: String): LinearLayout {
        val itemLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            background = if (isCompleted) {
                ContextCompat.getDrawable(requireContext(), R.drawable.exercise_card_bg)?.apply {
                    setColorFilter(Color.LTGRAY, android.graphics.PorterDuff.Mode.MULTIPLY)
                }
            } else {
                ContextCompat.getDrawable(requireContext(), R.drawable.exercise_card_bg)
            }
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
            setTextColor(if (isCompleted) Color.GRAY else Color.BLACK)
        }

        val setsLine = TextView(requireContext()).apply {
            text = "Sets: ${exercise.sets}"
            setTextColor(if (isCompleted) Color.GRAY else Color.parseColor("#555555"))
            textSize = 14f
            setPadding(0, 4, 0, 0)
        }

        val repsLine = TextView(requireContext()).apply {
            text = "Reps per Set: ${exercise.reps.joinToString(" / ")}"
            setTextColor(if (isCompleted) Color.GRAY else Color.parseColor("#555555"))
            textSize = 14f
            setPadding(0, 0, 0, 2)
        }

        val equipment = TextView(requireContext()).apply {
            text = "Equipment: ${exercise.equipment} | Rest: ${exercise.rest_time_seconds}s"
            textSize = 13f
            setTextColor(if (isCompleted) Color.GRAY else Color.GRAY)
        }

        // Add completion indicator
        if (isCompleted) {
            val completionIndicator = TextView(requireContext()).apply {
                text = "✅ Completed"
                textSize = 12f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 8, 0, 0)
            }
            itemLayout.addView(completionIndicator)
        }

        // Add long press to mark as complete (for testing)
        if (!isCompleted) {
            itemLayout.setOnLongClickListener {
                markExerciseAsCompleted(dayName, exercise.name)
                // Refresh the view to show completion
                refreshAllWorkoutDays(sampleWorkoutPlan?.training_split ?: emptyList())
                true
            }
        }

        itemLayout.addView(title)
        itemLayout.addView(setsLine)
        itemLayout.addView(repsLine)
        itemLayout.addView(equipment)

        return itemLayout
    }

    private fun markWorkoutDayAsCompleted(
        view: View, 
        startButton: Button, 
        dayTitle: TextView, 
        muscleGroup: TextView
    ) {
        // Disable start button
        startButton.isEnabled = false
        startButton.alpha = 0.5f
        startButton.text = "Completed ✓"

        // Gray out the entire day view
        view.alpha = 0.6f
        dayTitle.setTextColor(Color.GRAY)
        muscleGroup.setTextColor(Color.GRAY)

        // Add completion overlay
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

        val completionText = TextView(requireContext()).apply {
            text = "WORKOUT COMPLETE! 🎉"
            textSize = 16f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
            setTypeface(null, Typeface.BOLD)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
                topMargin = 140
            }
        }

        overlay.addView(checkIcon)
        overlay.addView(completionText)
        wrapper.addView(overlay)

        containerLayout.addView(wrapper)
    }

    private fun isExerciseCompleted(dayName: String, exerciseName: String): Boolean {
        val sharedPrefs = requireContext().getSharedPreferences("workout_completion", Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean("${dayName}_${exerciseName}", false)
    }

    private fun isWorkoutDayCompleted(day: TrainingDay): Boolean {
        if (day.exercises.isEmpty()) return false
        return day.exercises.all { exercise ->
            isExerciseCompleted(day.day, exercise.name)
        }
    }

    private fun markExerciseAsCompleted(dayName: String, exerciseName: String) {
        val sharedPrefs = requireContext().getSharedPreferences("workout_completion", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("${dayName}_${exerciseName}", true).apply()
        
        // Check if all exercises for this day are completed
        val day = sampleWorkoutPlan?.training_split?.find { it.day == dayName }
        day?.let {
            if (isWorkoutDayCompleted(it)) {
                // Refresh the view to show completion state
                refreshWorkoutDayView(it)
            }
        }
    }

    private fun resetAllWorkoutProgress() {
        val sharedPrefs = requireContext().getSharedPreferences("workout_completion", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()
        
        // Refresh the view to show reset state
        sampleWorkoutPlan?.training_split?.let { days ->
            refreshAllWorkoutDays(days)
        }
    }

    private fun refreshAllWorkoutDays(days: List<TrainingDay>) {
        // Clear current views and repopulate with updated completion status
        containerLayout.removeAllViews()
        populateWorkoutDays(days)
    }

    private fun refreshWorkoutDayView(day: TrainingDay) {
        // Find and update the existing view for this day
        for (i in 0 until containerLayout.childCount) {
            val child = containerLayout.getChildAt(i)
            if (child is FrameLayout && child.childCount > 0) {
                val dayView = child.getChildAt(0)
                val dayTitle = dayView.findViewById<TextView>(R.id.dayTitle)
                if (dayTitle?.text == day.day) {
                    // Remove the old view and recreate with completion state
                    containerLayout.removeView(child)
                    populateWorkoutDays(listOf(day))
                    break
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