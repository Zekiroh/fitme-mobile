package com.samsantech.fitme.workouts

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.samsantech.fitme.R
import androidx.core.graphics.toColorInt
import com.samsantech.fitme.api.RetrofitClient
import com.samsantech.fitme.components.SharedPrefHelper
import com.samsantech.fitme.model.AddWorkoutResponse
import com.samsantech.fitme.model.WorkoutInput
import com.samsantech.fitme.model.CustomWorkoutRequest
import com.samsantech.fitme.model.CustomWorkoutResponse
import com.samsantech.fitme.workouts.WorkoutCompletion
import com.samsantech.fitme.workouts.SharedWorkoutViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomWorkoutFragment : Fragment() {

    companion object {
        private const val ARG_EXERCISES = "exercises"

        fun newInstance(exercises: List<CustomExercise>): CustomWorkoutFragment {
            val fragment = CustomWorkoutFragment()
            val args = Bundle()
            args.putParcelableArrayList(ARG_EXERCISES, ArrayList(exercises))
            fragment.arguments = args
            return fragment
        }
    }

    private var exercises: List<CustomExercise>? = null
    private val sharedViewModel: SharedWorkoutViewModel by activityViewModels()

    private var workoutSeconds = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var workoutTimerRunnable: Runnable
    private var isWorkoutCompleting = false
    private lateinit var btnFinish: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            exercises = it.getParcelableArrayList(ARG_EXERCISES)
        }
    }

    @SuppressLint("UseKtx")
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
            text = "Custom Workout"
            textSize = 22f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }
        layout.addView(title)

        val workoutTimerText = TextView(requireContext()).apply {
            "Workout Duration: 00:00".also { text = it }
            textSize = 18f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 16, 0, 24)
            layoutParams = params
        }
        layout.addView(workoutTimerText)

        workoutTimerRunnable = object : Runnable {
            @SuppressLint("DefaultLocale")
            override fun run() {
                workoutSeconds++
                val minutes = workoutSeconds / 60
                val seconds = workoutSeconds % 60
                String.format("Workout Duration: %02d:%02d", minutes, seconds)
                    .also { workoutTimerText.text = it }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(workoutTimerRunnable)

        val enabledDrawable = GradientDrawable().apply {
            cornerRadius = 32f
            setColor("#FF7F50".toColorInt())
        }
        val disabledDrawable = GradientDrawable().apply {
            cornerRadius = 32f
            setColor("#CCCCCC".toColorInt())
        }

        btnFinish = Button(requireContext()).apply {
            "Finish Workout".also { text = it }
            setTextColor(Color.WHITE)
            background = disabledDrawable
            isEnabled = false
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 32, 0, 0)
            layoutParams = params
        }

        val doneCountsList = mutableListOf<Pair<IntArray, Int>>()

        exercises?.forEach { exercise ->
            val exerciseContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                val drawable = GradientDrawable()
                drawable.setColor(Color.WHITE)
                drawable.cornerRadius = 24f
                background = drawable
                setPadding(24, 24, 24, 24)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 24)
                layoutParams = params
                elevation = 10f
            }

            val headerLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER_VERTICAL
            }

            val exerciseTitle = TextView(requireContext()).apply {
                text = exercise.name
                textSize = 18f
                setTextColor(Color.BLACK)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val exerciseInfo = TextView(requireContext()).apply {
                text = "${exercise.sets} sets × ${exercise.reps} reps"
                textSize = 14f
                setTextColor(Color.DKGRAY)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val toggleIcon = TextView(requireContext()).apply {
                text = "▼"
                textSize = 18f
                setTextColor(Color.BLACK)
            }

            headerLayout.addView(exerciseTitle)
            headerLayout.addView(exerciseInfo)
            headerLayout.addView(toggleIcon)

            exerciseContainer.addView(headerLayout)

            val contentLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                visibility = View.GONE
            }

            val setsLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
            }

            val totalSets = exercise.sets
            val doneCount = intArrayOf(0)

            doneCountsList.add(Pair(doneCount, totalSets))

            for (i in 1..totalSets) {
                val setRow = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 12, 0, 12)
                    layoutParams = params
                }

                val setNumber = TextView(requireContext()).apply {
                    text = "$i"
                    textSize = 16f
                    setTextColor(Color.DKGRAY)
                }

                val weightInput = EditText(requireContext()).apply {
                    hint = "Weight"
                    setText("60")
                    inputType = android.text.InputType.TYPE_CLASS_NUMBER
                    setBackgroundColor(Color.parseColor("#F5F5F5"))
                    setPadding(16, 8, 16, 8)
                    val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    params.setMargins(8, 0, 8, 0)
                    layoutParams = params
                }

                val repsInput = EditText(requireContext()).apply {
                    hint = "Reps"
                    setText(exercise.reps.toString())
                    inputType = android.text.InputType.TYPE_CLASS_NUMBER
                    setBackgroundColor(Color.parseColor("#F5F5F5"))
                    setPadding(16, 8, 16, 8)
                    val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    params.setMargins(8, 0, 8, 0)
                    layoutParams = params
                }

                val doneCheck = CheckBox(requireContext()).apply {
                    buttonTintList = ColorStateList.valueOf(Color.parseColor("#FF7F50"))
                    setOnCheckedChangeListener { buttonView, isChecked ->
                        if (isChecked) {
                            if (weightInput.text.isNullOrEmpty() || repsInput.text.isNullOrEmpty()) {
                                Toast.makeText(requireContext(), "Please enter weight and reps first!", Toast.LENGTH_SHORT).show()
                                buttonView.isChecked = false
                            } else {
                                doneCount[0]++
                                isEnabled = false
                                weightInput.isEnabled = false
                                repsInput.isEnabled = false

                                var allDone = true
                                for (pair in doneCountsList) {
                                    if (pair.first[0] < pair.second) {
                                        allDone = false
                                        break
                                    }
                                }
                                btnFinish.isEnabled = allDone
                                btnFinish.background = if (allDone) enabledDrawable else disabledDrawable
                                
                                val user = SharedPrefHelper.getLoggedInUser(requireContext())
                                user?.let {
                                    RetrofitClient.members.addWorkout(it.id, WorkoutInput(
                                        weight = weightInput.text.toString(),
                                        reps = repsInput.text.toString(),
                                        description = exerciseTitle.text.toString()
                                    )).enqueue(object: Callback<AddWorkoutResponse> {
                                        override fun onResponse(
                                            call: Call<AddWorkoutResponse?>,
                                            response: Response<AddWorkoutResponse?>
                                        ) {
                                            val workouts = response.body()
                                            Log.e("active workout ${workouts?.message}", "Error: ${response.code()}")
                                        }

                                        override fun onFailure(
                                            call: Call<AddWorkoutResponse?>,
                                            t: Throwable
                                        ) {
                                            Log.e("add to workout", "Failed: ${t.localizedMessage}")
                                        }
                                    })
                                }

                                if (doneCount[0] == totalSets) {
                                    Toast.makeText(requireContext(), "${exercise.name} completed!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }

                setRow.addView(setNumber)
                setRow.addView(weightInput)
                setRow.addView(repsInput)
                setRow.addView(doneCheck)
                setsLayout.addView(setRow)
            }

            contentLayout.addView(setsLayout)

            val timerText = TextView(requireContext()).apply {
                "Rest: Not started".also { text = it }
                textSize = 16f
                setTextColor(Color.DKGRAY)
                gravity = Gravity.CENTER_HORIZONTAL
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 16, 0, 0)
                layoutParams = params
            }

            val btnStartRest = Button(requireContext()).apply {
                "Start Rest (${exercise.restTimeSeconds}s)".also { text = it }
                setBackgroundColor(Color.parseColor("#FF7F50"))
                setTextColor(Color.WHITE)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 8, 0, 0)
                layoutParams = params
            }

            btnStartRest.setOnClickListener {
                btnStartRest.isEnabled = false
                object : CountDownTimer(exercise.restTimeSeconds * 1000L, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val secondsLeft = millisUntilFinished / 1000
                        "Rest: $secondsLeft sec remaining".also { timerText.text = it }
                    }

                    override fun onFinish() {
                        "Rest complete!".also { timerText.text = it }
                        btnStartRest.isEnabled = true
                        timerText.setTextColor(Color.parseColor("#FF7F50"))
                    }
                }.start()
            }

            contentLayout.addView(timerText)
            contentLayout.addView(btnStartRest)

            exerciseContainer.addView(contentLayout)

            headerLayout.setOnClickListener {
                if (contentLayout.visibility == View.GONE) {
                    contentLayout.visibility = View.VISIBLE
                    toggleIcon.text = "▲"
                } else {
                    contentLayout.visibility = View.GONE
                    toggleIcon.text = "▼"
                }
            }

            layout.addView(exerciseContainer)
        }

        btnFinish.setOnClickListener {
            // Prevent double-clicking
            if (isWorkoutCompleting) {
                return@setOnClickListener
            }
            
            // Set flag to prevent multiple submissions
            isWorkoutCompleting = true
            
            // Disable the button to prevent further clicks
            btnFinish.isEnabled = false
            btnFinish.text = "Completing..."
            
            // Set a safety timeout to reset the state if something goes wrong
            handler.postDelayed({
                if (isWorkoutCompleting) {
                    resetWorkoutCompletionState()
                }
            }, 10000) // 10 second timeout
            
            // Save workout to API
            saveWorkoutToApi()
            
            // Update ViewModel with completion status
            exercises?.let { exerciseList ->
                val completion = WorkoutCompletion(
                    isCompleted = true,
                    completionTime = System.currentTimeMillis(),
                    workoutDetails = exerciseList.joinToString(",") { "${it.name}:${it.sets}×${it.reps}" },
                    duration = workoutSeconds,
                    exercises = exerciseList
                )
                sharedViewModel.markWorkoutAsCompleted(completion)
            }
            
            // Show completion success message
            Toast.makeText(requireContext(), "Workout Completed! 🎉", Toast.LENGTH_LONG).show()
            
            // Stop timer and return to custom tab
            handler.removeCallbacks(workoutTimerRunnable)
            
            // Navigate back to custom tab
            requireActivity().supportFragmentManager.popBackStack()
        }

        layout.addView(btnFinish)
        scrollView.addView(layout)
        return scrollView
    }



    private fun saveWorkoutToApi() {
        val user = SharedPrefHelper.getLoggedInUser(requireContext())
        user?.let { userData ->
            var successCount = 0
            val totalExercises = exercises?.size ?: 0
            
            exercises?.forEach { exercise ->
                val request = CustomWorkoutRequest(
                    rest = exercise.restTimeSeconds.toString(),
                    weight = "0", // Default weight since we don't track it in custom exercises
                    reps = exercise.reps.toString(),
                    description = exercise.name
                )
                
                RetrofitClient.aiFitness.saveCustomWorkout(userData.id, request)
                    .enqueue(object : Callback<CustomWorkoutResponse> {
                        override fun onResponse(
                            call: Call<CustomWorkoutResponse?>,
                            response: Response<CustomWorkoutResponse?>
                        ) {
                            if (response.isSuccessful && response.body() != null) {
                                Log.d("CustomWorkout", "Workout saved successfully: ${response.body()?.message}")
                                successCount++
                                
                                // Check if all exercises were saved successfully
                                if (successCount == totalExercises) {
                                    Log.d("CustomWorkout", "All workouts saved successfully to API")
                                }
                            } else {
                                Log.e("CustomWorkout", "Failed to save workout: ${response.code()}")
                                // Reset flag on API failure
                                resetWorkoutCompletionState()
                            }
                        }

                        override fun onFailure(call: Call<CustomWorkoutResponse?>, t: Throwable) {
                            Log.e("CustomWorkout", "Error saving workout: ${t.localizedMessage}")
                            // Reset flag on API failure
                            resetWorkoutCompletionState()
                        }
                    })
            }
        } ?: run {
            // No user found, reset state
            resetWorkoutCompletionState()
        }
    }

    private fun resetWorkoutCompletionState() {
        // Reset the completion flag
        isWorkoutCompleting = false
        
        // Re-enable the button
        btnFinish.isEnabled = true
        btnFinish.text = "Finish Workout"
        
        // Show error message to user
       // Toast.makeText(requireContext(), "Failed to save workout. Please try again.", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(workoutTimerRunnable)
        
        // Clear any pending completion timeout
        handler.removeCallbacksAndMessages(null)
    }
}
