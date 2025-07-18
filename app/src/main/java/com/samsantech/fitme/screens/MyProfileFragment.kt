package com.samsantech.fitme.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.samsantech.fitme.R
import com.samsantech.fitme.components.SharedPrefHelper

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
            val user = SharedPrefHelper.getLoggedInUser(ctx)
            user?.let {
                val userProfile = UserProfile(
                    name = it.fullName,
                    gender = it.gender,
                    username = it.username,
                    email = it.email,
                    fitnessLevel = it.frequency.toString(),
                    fitnessGoal = "N/A",
                    weight = "${it.weight} kg",
                    height = "${it.height} cm",
                    bmi = "N/A"
                )

                tvName.text = userProfile.name
                tvGender.text = userProfile.gender
                tvUsername.text = userProfile.username
                tvEmail.text = userProfile.email
                tvFitnessLevel.text = userProfile.fitnessLevel
                tvFitnessGoal.text = userProfile.fitnessGoal
                tvWeight.text = userProfile.weight
                tvHeight.text = userProfile.height
                tvBMI.text = userProfile.bmi
            }
        }
    }
}
