package com.samsantech.fitme.screens

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

data class UserProfile(
    val name: String,
    val gender: String,
    val username: String,
    val email: String,
    val fitnessLevel: String,
    val fitnessGoal: String,
    val weight: String,
    val height: String,
    val bmi: String
)

class MyProfileFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvContact: TextView
    private lateinit var tvFitnessLevel: TextView
    private lateinit var tvFitnessGoal: TextView
    private lateinit var tvWeight: TextView
    private lateinit var tvHeight: TextView
    private lateinit var tvBMI: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_profilee, container, false)

        // Bind views to IDs in XML
        tvName = view.findViewById(R.id.tvName)
        tvGender = view.findViewById(R.id.tvGender)
        tvUsername = view.findViewById(R.id.tvUsername)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvFitnessLevel = view.findViewById(R.id.tvFitnessLevel)
        tvFitnessGoal = view.findViewById(R.id.tvFitnessGoal)
        tvWeight = view.findViewById(R.id.tvWeight)
        tvHeight = view.findViewById(R.id.tvHeight)
        tvBMI = view.findViewById(R.id.tvBMI)

        loadUserProfile()

        return view
    }

    private fun loadUserProfile() {
        // TODO: Replace this with your API or database fetch, brylle ikaw na dito need ifetch yung data sa db papunta dito para mag work yung actual credentials ng user
        context?.let { ctx ->
            val sharedPref = ctx.getSharedPreferences("usersInfo", Context.MODE_PRIVATE)
            val userJson = sharedPref.getString("user_data", null)
            val user = Gson().fromJson(userJson, User::class.java)
            // TODO: Replace fakeProfile with actual deserialization if needed
            val fakeProfile = UserProfile(
                name = user.fullName,
                gender = user.gender,
                username = user.username,
                email = user.email,
                fitnessLevel = user.frequency.toString(),
                fitnessGoal = "N/A",
                weight = "${user.weight} kg",
                height = "${user.height} cm",
                bmi = "N/A"
            )

            // Populate text boxes
            tvName.text = fakeProfile.name
            tvGender.text = fakeProfile.gender
            tvUsername.text = fakeProfile.username
            tvEmail.text = fakeProfile.email
            tvFitnessLevel.text = fakeProfile.fitnessLevel
            tvFitnessGoal.text = fakeProfile.fitnessGoal
            tvWeight.text = fakeProfile.weight
            tvHeight.text = fakeProfile.height
            tvBMI.text = fakeProfile.bmi
        }
    }
}
