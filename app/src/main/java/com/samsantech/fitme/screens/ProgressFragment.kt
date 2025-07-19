package com.samsantech.fitme.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.samsantech.fitme.R
import com.samsantech.fitme.api.RetrofitClient
import com.samsantech.fitme.components.SharedPrefHelper
import com.samsantech.fitme.model.CoachNote
import com.samsantech.fitme.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.graphics.toColorInt
import com.samsantech.fitme.auth.AccountInfoActivity
import com.samsantech.fitme.model.WeeklyWorkoutResponse
import com.samsantech.fitme.onboarding.AssessmentGenderActivity
import com.samsantech.fitme.workouts.ActivityGetRecordsWorkout
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.absoluteValue

class ProgressFragment : Fragment() {

    @SuppressLint("MissingInflatedId")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progress, container, false)

        // Find TextViews by ID
        val textWorkouts = view.findViewById<TextView>(R.id.textWorkouts)
        val textMinutes = view.findViewById<TextView>(R.id.textMinutes)
        val welcomeIdText = view.findViewById<TextView>(R.id.welcomeUser)
        val allRecordsView = view.findViewById<TextView>(R.id.allRecords)
        // (Optional future) val textKcal = view.findViewById<TextView>(R.id.textKcal)

        // Read from SharedPreferences
        val sharedPrefs = requireActivity().getSharedPreferences("FitMePrefs", Context.MODE_PRIVATE)
        val workouts = sharedPrefs.getInt("workouts_count", 0)
        val minutes = sharedPrefs.getInt("total_minutes", 0)
        val sharedPref = context?.getSharedPreferences("usersInfo", Context.MODE_PRIVATE)
        val userJson = sharedPref?.getString("user_data", null)
        val user = Gson().fromJson(userJson, User::class.java)

        val welcomeView = user.username
        // val kcal = sharedPrefs.getInt("total_kcal", 0)
        // Update UI
        textWorkouts.text = workouts.toString()
        textMinutes.text = minutes.toString()
        "Welcome ($welcomeView)".also { welcomeIdText.text = it }
        // textKcal.text = kcal.toString()
        allRecordsView.setOnClickListener {
            val intent = Intent(requireContext(), ActivityGetRecordsWorkout::class.java)
            startActivity(intent)
        }
        return view
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialCoachNode()
        historyWorkOut()
    }


    @SuppressLint("DefaultLocale")
    private fun initialCoachNode () {
        val user = SharedPrefHelper.getLoggedInUser(context)
        user?.let {
            if(user.weight > 0) {
                val weightTextView = view?.findViewById<TextView>(R.id.weightView)
                val bmiTextView = view?.findViewById<TextView>(R.id.bmiText)
                val heightInMeters = user.height / 100.0
                val bmi = user.weight / (heightInMeters * heightInMeters)
                weightTextView?.visibility = View.VISIBLE
                bmiTextView?.visibility = View.VISIBLE
                String.format("%.2f", bmi).also { bmiTextView?.text = it }
                "${it.weight} KG".also { weightTextView?.text = it }
//                "${it.} KG".also { bmiTextView?.text = it }
            } else {
                val weightButton = view?.findViewById<Button>(R.id.weightButton)
                val bmiButton = view?.findViewById<Button>(R.id.bmiButton)
                weightButton?.visibility = View.VISIBLE
                bmiButton?.visibility = View.VISIBLE
            }

            RetrofitClient.members.getCouchNotes(it.id)
                .enqueue(object: Callback<CoachNote> {
                    override fun onResponse(
                        call: Call<CoachNote?>,
                        response: Response<CoachNote?>
                    ) {
                        if(response.isSuccessful) {
                            val couch = response.body()
                            val coachNoteText = view?.findViewById<TextView>(R.id.coachNote)
                            coachNoteText?.text = couch?.coachNotes ?: ""
                        }
                    }

                    override fun onFailure(
                        call: Call<CoachNote?>,
                        t: Throwable
                    ) {
                        Log.e("MembershipDetails", "Failed: ${t.message}")
                    }

                })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun historyWorkOut() {
        val layoutAddTexts = view?.findViewById<LinearLayout>(R.id.recordHistory)
        val loadingText = view?.findViewById<ProgressBar>(R.id.progressBar)
        layoutAddTexts?.removeAllViews()
        loadingText?.visibility = View.VISIBLE
        val dateFormat = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)

        val user = SharedPrefHelper.getLoggedInUser(requireContext())

        user?.let { dt ->
            RetrofitClient.members.getWorkoutWeekly(dt.id)
                .enqueue(object : Callback<WeeklyWorkoutResponse> {
                    override fun onResponse(
                        call: Call<WeeklyWorkoutResponse?>,
                        response: Response<WeeklyWorkoutResponse?>
                    ) {
                        loadingText?.visibility = View.GONE
                        val workouts = response.body()?.workouts ?: emptyList()

                        val workoutDates = workouts.mapNotNull {
                            try {
                                LocalDate.parse(it.date, dateFormat)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (workoutDates.isEmpty()) return

                        // Start of week based on earliest workout date
                        val minDate = workoutDates.minOrNull() ?: LocalDate.now()
                        val startOfWeek = minDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

                        // Display the week (7 days from Sunday to Saturday)
                        for (i in 0..6) {
                            val date = startOfWeek.plusDays(i.toLong())

                            val circleDayView = layoutInflater.inflate(
                                R.layout.text_dynamic_circle_view, layoutAddTexts, false
                            ) as TextView

                            circleDayView.text = date.dayOfMonth.toString()
                            circleDayView.textSize = 16f
                            val colors = listOf(
                                "#FF5733".toColorInt(), // Red-Orange
                                "#33FF57".toColorInt(), // Green
                                "#3357FF".toColorInt(), // Blue
                                "#F1C40F".toColorInt(), // Yellow
                                "#9B59B6".toColorInt(), // Purple
                                "#1ABC9C".toColorInt(), // Teal
                                "#E67E22".toColorInt()  // Orange
                            )

                            if (workoutDates.contains(date)) {
                                // Highlight workout date
                                val background = ContextCompat.getDrawable(
                                    requireContext(), R.drawable.circle_background
                                )?.mutate()

                                if (background is GradientDrawable) {
                                    val colorIndex = (date.hashCode().absoluteValue % colors.size)
                                    val color = colors[colorIndex]

                                    background.setColor(color)
                                    circleDayView.background = background
                                    circleDayView.setTextColor(Color.WHITE)
                                }
                            } else {
                                // Not a workout date
                                circleDayView.background = null
                                circleDayView.setTextColor("#000000".toColorInt())
                            }


                            layoutAddTexts?.addView(circleDayView)
                        }
                    }

                    override fun onFailure(call: Call<WeeklyWorkoutResponse?>, t: Throwable) {
                        loadingText?.visibility = View.GONE
                        Log.e("WorkoutFetch", "Error fetching weekly workouts", t)
                    }
                })
        }



    }
}