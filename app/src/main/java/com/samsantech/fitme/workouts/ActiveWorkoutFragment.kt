package com.samsantech.fitme.workouts

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.samsantech.fitme.R

class ActiveWorkoutFragment : Fragment() {

    companion object {
        private const val ARG_GROUP_NAME = "group_name"
        private const val ARG_EXERCISES = "exercises"

        fun newInstance(groupName: String, exercises: ArrayList<String>): ActiveWorkoutFragment {
            val fragment = ActiveWorkoutFragment()
            val args = Bundle()
            args.putString(ARG_GROUP_NAME, groupName)
            args.putStringArrayList(ARG_EXERCISES, exercises)
            fragment.arguments = args
            return fragment
        }
    }

    private var groupName: String? = null
    private var exercises: ArrayList<String>? = null

    private var workoutSeconds = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var workoutTimerRunnable: Runnable

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
            text = groupName
            textSize = 22f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }
        layout.addView(title)

        val workoutTimerText = TextView(requireContext()).apply {
            text = "Workout Duration: 00:00"
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
            override fun run() {
                workoutSeconds++
                val minutes = workoutSeconds / 60
                val seconds = workoutSeconds % 60
                workoutTimerText.text = String.format("Workout Duration: %02d:%02d", minutes, seconds)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(workoutTimerRunnable)

        val enabledDrawable = GradientDrawable().apply {
            cornerRadius = 32f
            setColor(Color.parseColor("#FF7F50"))
        }
        val disabledDrawable = GradientDrawable().apply {
            cornerRadius = 32f
            setColor(Color.parseColor("#CCCCCC"))
        }

        val btnFinish = Button(requireContext()).apply {
            text = "Finish Workout"
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
                text = exercise
                textSize = 18f
                setTextColor(Color.BLACK)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val toggleIcon = TextView(requireContext()).apply {
                text = "▼"
                textSize = 18f
                setTextColor(Color.BLACK)
            }

            headerLayout.addView(exerciseTitle)
            headerLayout.addView(toggleIcon)

            exerciseContainer.addView(headerLayout)

            val contentLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                visibility = View.GONE
            }

            val setsLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
            }

            val totalSets = 4
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
                    inputType = android.text.InputType.TYPE_CLASS_NUMBER
                    setBackgroundColor(Color.parseColor("#F5F5F5"))
                    setPadding(16, 8, 16, 8)
                    val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    params.setMargins(8, 0, 8, 0)
                    layoutParams = params
                }

                val repsInput = EditText(requireContext()).apply {
                    hint = "Reps"
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

                                if (doneCount[0] == totalSets) {
                                    Toast.makeText(requireContext(), "$exercise completed!", Toast.LENGTH_SHORT).show()
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
                text = "Rest: Not started"
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
                text = "Start Rest (30s)"
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
                object : CountDownTimer(30000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val secondsLeft = millisUntilFinished / 1000
                        timerText.text = "Rest: $secondsLeft sec remaining"
                    }

                    override fun onFinish() {
                        timerText.text = "Rest complete!"
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
            Toast.makeText(requireContext(), "Workout Finished!", Toast.LENGTH_SHORT).show()
            handler.removeCallbacks(workoutTimerRunnable)
            requireActivity().supportFragmentManager.popBackStack()
        }

        layout.addView(btnFinish)
        scrollView.addView(layout)
        return scrollView
    }
}