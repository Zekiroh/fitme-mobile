package com.samsantech.fitme.workouts

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.samsantech.fitme.R
import com.samsantech.fitme.model.Exercise

class WorkoutSessionActivity : AppCompatActivity() {
    private lateinit var container: LinearLayout
    private var completedCount = 0
    private var totalExercises = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_workout_session)

        container = findViewById(R.id.exerciseSessionContainer)

        val exercises = intent.getSerializableExtra("exercises") as? ArrayList<Exercise>
        exercises?.let {
            totalExercises = it.size
            renderExercises(it)
        }

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            showConfirmExitDialog()
        }

    }

    private fun renderExercises(exerciseList: List<Exercise>) {
         for ((index, exercise) in exerciseList.withIndex()) {
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
                background = ContextCompat.getDrawable(this@WorkoutSessionActivity, R.drawable.exercise_card_bg)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 24)
                layoutParams = params
                tag = "exercise_$index" // useful for updates
            }

            val title = TextView(this).apply {
                "• ${exercise.name}".also { text = it }
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.BLACK)
            }

            val reps = TextView(this).apply {
                "Reps per Set: ${exercise.reps.joinToString(" → ")}".also { text = it }
                setTextColor(Color.DKGRAY)
                textSize = 14f
            }

            val rest = TextView(this).apply {
                "Rest Time: ${exercise.rest_time_seconds}s".also { text = it }
                textSize = 13f
                setTextColor(Color.GRAY)
            }

            val doneButton = Button(this).apply {
                text = "Done"
                setTextColor(Color.WHITE)
                setOnClickListener {
                    isEnabled = false
                    text = "Resting..."
                    startRestTimer(this, exercise.rest_time_seconds, itemLayout)
                }
            }

            itemLayout.addView(title)
            itemLayout.addView(reps)
            itemLayout.addView(rest)
            itemLayout.addView(doneButton)

            container.addView(itemLayout)
        }
    }

    private fun startRestTimer(button: Button, seconds: Int, cardLayout: LinearLayout) {
        object : CountDownTimer(seconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                "Resting: ${millisUntilFinished / 1000}s".also { button.text = it }
            }

            override fun onFinish() {
                markExerciseAsDone(button, cardLayout)
                completedCount++
                if (completedCount == totalExercises) {
                    Toast.makeText(this@WorkoutSessionActivity, "Workout complete! 👏", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }.start()
    }
    private fun markExerciseAsDone(button: Button, cardLayout: LinearLayout) {
        cardLayout.setBackgroundColor(Color.LTGRAY)
        "Completed".also { button.text = it }
        button.setTextColor(Color.DKGRAY)
        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check, 0, 0, 0)
    }

    override fun onBackPressed() {
        if (completedCount > totalExercises) {
            super.onBackPressed()
        } else {
            showConfirmExitDialog()
        }
    }

    private fun showConfirmExitDialog() {
        if (completedCount < totalExercises) {
            val dialog = android.app.AlertDialog.Builder(this)
                .setTitle("Exit Workout")
                .setMessage("You haven’t completed all exercises. Exit anyway?")
                .setPositiveButton("Yes") { _, _ -> finish() }
                .setNegativeButton("No", null)
                .create()
            dialog.show()
        } else {
            finish()
        }
    }
}