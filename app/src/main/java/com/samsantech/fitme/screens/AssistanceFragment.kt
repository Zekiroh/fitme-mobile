package com.samsantech.fitme.screens

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.samsantech.fitme.R
import com.samsantech.fitme.api.RetrofitClient
import com.samsantech.fitme.model.AssistanceRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AssistanceFragment : Fragment() {

    private lateinit var dropdownCategory: AutoCompleteTextView
    private lateinit var etDescription: EditText
    private lateinit var btnSubmit: Button
    private lateinit var devInfoIcon: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_assistance, container, false)

        // Bind views
        dropdownCategory = view.findViewById(R.id.dropdownCategory)
        etDescription = view.findViewById(R.id.etDescription)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        devInfoIcon = view.findViewById(R.id.devInfoIcon)
        val gymContact = view.findViewById<TextView>(R.id.textGymContact)
        val devContact = view.findViewById<TextView>(R.id.textDevContact)
        val btnViewHistory = view.findViewById<Button>(R.id.btnViewHistory)
        val welcomeText = view.findViewById<TextView>(R.id.textWelcomeName)

        // Set full name
        val sharedPref = requireContext().getSharedPreferences("FitMePrefs", Context.MODE_PRIVATE)
        val fullName = sharedPref.getString("full_name", "[Full Name]")
        welcomeText.text = "Welcome, $fullName"

        // Navigate to history
        btnViewHistory.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, AssistanceHistoryFragment())
                .addToBackStack(null)
                .commit()
        }

        // Make contact links clickable
        Linkify.addLinks(gymContact, Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS)
        Linkify.addLinks(devContact, Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS)
        gymContact.setLinkTextColor(Color.parseColor("#4B5563"))
        devContact.setLinkTextColor(Color.parseColor("#4B5563"))

        // Dropdown options
        val categories = listOf(
            "Workout Assistance",
            "Equipment Issue",
            "Billing Concern",
            "Technical Problem",
            "Other"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        dropdownCategory.setAdapter(adapter)

        dropdownCategory.setOnClickListener {
            dropdownCategory.showDropDown()
        }

        // Submit assistance request
        btnSubmit.setOnClickListener {
            val category = dropdownCategory.text.toString().trim()
            val message = etDescription.text.toString().trim()

            if (category.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a category.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (message.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your message.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = sharedPref.getInt("user_id", -1)

            if (userId == -1) {
                Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = AssistanceRequest(userId, category, message)

            RetrofitClient.assistance.submitAssistance(request)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            showSuccessDialog(category, message)
                            clearForm()
                        } else {
                            Toast.makeText(requireContext(), "Submission failed.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        devInfoIcon.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_dev_info, null)
            AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .show()
        }

        return view
    }

    private fun showSuccessDialog(category: String, message: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_success_assistance, null)
        val detailsText = dialogView.findViewById<TextView>(R.id.textSuccessDetails)
        val btnDismiss = dialogView.findViewById<Button>(R.id.btnDismiss)

        detailsText.text = "• Category: $category\n• Message: $message"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnDismiss.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun clearForm() {
        dropdownCategory.setText("")
        etDescription.text.clear()
    }
}