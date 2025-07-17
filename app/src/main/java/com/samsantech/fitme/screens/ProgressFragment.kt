package com.samsantech.fitme.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.samsantech.fitme.R
import com.samsantech.fitme.model.User

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
        welcomeIdText.text = "Welcome ($welcomeView)"
        // textKcal.text = kcal.toString()

        return view
    }
}