package com.samsantech.fitme.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.samsantech.fitme.R
import com.samsantech.fitme.api.RetrofitClient
import com.samsantech.fitme.auth.MembershipUpgradeActivity
import com.samsantech.fitme.components.SharedPrefHelper
import com.samsantech.fitme.model.MembershipPlan
import com.samsantech.fitme.model.MembershipUpgradeResponse
import com.samsantech.fitme.utils.ApiTestHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MembershipDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_membership_detailss, container, false)
    }

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty() || dateString == "N/A") {
            return "N/A"
        }
        
        Log.d("DateFormat", "Attempting to format date: '$dateString'")
        
        return try {
            // Try to parse common date formats
            val inputFormats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",  // 2026-03-01T16:00:00.000Z
                "yyyy-MM-dd'T'HH:mm:ss'Z'",      // 2026-03-01T16:00:00Z
                "yyyy-MM-dd'T'HH:mm:ss.SSS",     // 2026-03-01T16:00:00.000
                "yyyy-MM-dd'T'HH:mm:ss",         // 2026-03-01T16:00:00
                "yyyy-MM-dd HH:mm:ss",           // 2026-03-01 16:00:00
                "yyyy-MM-dd",                    // 2026-03-01
                "MM/dd/yyyy",                    // 03/01/2026
                "dd/MM/yyyy"                     // 01/03/2026
            )
            
            var parsedDate: Date? = null
            var usedFormat: String? = null
            
            for (format in inputFormats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.getDefault())
                    // Set timezone to UTC for 'Z' format
                    if (format.contains("'Z'")) {
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                    }
                    parsedDate = sdf.parse(dateString)
                    if (parsedDate != null) {
                        usedFormat = format
                        Log.d("DateFormat", "Successfully parsed '$dateString' using format '$format'")
                        break
                    }
                } catch (e: Exception) {
                    Log.d("DateFormat", "Failed to parse '$dateString' with format '$format': ${e.message}")
                    // Continue to next format
                }
            }
            
            if (parsedDate != null) {
                val now = Date()
                val diffInMillis = parsedDate.time - now.time
                val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)
                
                // Format to user-friendly display with relative time
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val formattedDate = outputFormat.format(parsedDate)
                
                when {
                    diffInDays < 0 -> "$formattedDate (Expired)"
                    diffInDays == 0L -> "$formattedDate (Expires today)"
                    diffInDays == 1L -> "$formattedDate (1 day remaining)"
                    diffInDays < 7 -> "$formattedDate ($diffInDays days remaining)"
                    diffInDays < 30 -> "$formattedDate (${diffInDays / 7} weeks remaining)"
                    else -> "$formattedDate (${diffInDays / 30} months remaining)"
                }
            } else {
                // If parsing fails, try one more approach for ISO 8601 with Z
                Log.d("DateFormat", "All formats failed, trying manual parsing for: $dateString")
                try {
                    // Handle the specific case: 2026-03-01T16:00:00.000Z
                    if (dateString.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z"))) {
                        val cleanDate = dateString.replace("T", " ").replace("Z", "").substring(0, 19)
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        val parsedDate = sdf.parse(cleanDate)
                        if (parsedDate != null) {
                            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            val formattedDate = outputFormat.format(parsedDate)
                            val now = Date()
                            val diffInMillis = parsedDate.time - now.time
                            val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)
                            
                            return when {
                                diffInDays < 0 -> "$formattedDate (Expired)"
                                diffInDays == 0L -> "$formattedDate (Expires today)"
                                diffInDays == 1L -> "$formattedDate (1 day remaining)"
                                diffInDays < 7 -> "$formattedDate ($diffInDays days remaining)"
                                diffInDays < 30 -> "$formattedDate (${diffInDays / 7} weeks remaining)"
                                else -> "$formattedDate (${diffInDays / 30} months remaining)"
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DateFormat", "Manual parsing also failed: ${e.message}")
                }
                
                // If all parsing fails, return original string
                dateString
            }
        } catch (e: Exception) {
            Log.e("DateFormat", "Error formatting date: $dateString", e)
            dateString
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchMembershipDetails()
        setupChangePlanButton()
        
        // Check if this fragment was loaded after an upgrade to show updated data
        val refreshMembershipDetails = requireActivity().intent.getBooleanExtra("refreshMembershipDetails", false)
        if (refreshMembershipDetails) {
            // Show a toast to indicate membership was updated
            android.widget.Toast.makeText(requireContext(), "Membership updated! Refreshing details...", android.widget.Toast.LENGTH_SHORT).show()
            
            // Add a small delay to ensure the backend has processed the upgrade
            view.postDelayed({
                fetchMembershipDetails()
            }, 1000)
        }
    }

    private fun setupChangePlanButton() {
        val changePlanButton = view?.findViewById<Button>(R.id.button_change_plan)
        changePlanButton?.setOnClickListener {
            val intent = Intent(requireContext(), MembershipUpgradeActivity::class.java)
            startActivity(intent)
        }
        
        // Add long click listener for API testing (development only)
        changePlanButton?.setOnLongClickListener {
            testApiIntegration()
            true
        }
    }
    
    private fun testApiIntegration() {
        val user = SharedPrefHelper.getLoggedInUser(context)
        user?.let {
            Log.d("API_TEST", "Testing API integration for user: ${it.id}")
            
            // Test get current membership
            ApiTestHelper.testGetCurrentMembershipAPI(it.id)
            
            // Test get plans
            ApiTestHelper.testGetPlansAPI()
            
            Log.d("API_TEST", "API tests initiated. Check logs for results.")
        }
    }

    private fun fetchMembershipDetails() {
        val user = SharedPrefHelper.getLoggedInUser(context)
        user?.let {
            // Try the new API endpoint first
            RetrofitClient.payments.getCurrentMembership(it.id)
                .enqueue(object : Callback<MembershipUpgradeResponse> {
                    override fun onResponse(
                        call: Call<MembershipUpgradeResponse>,
                        response: Response<MembershipUpgradeResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            val membershipData = response.body()?.data
                            val valuePlan = view?.findViewById<TextView>(R.id.value_plan)
                            val memberShip = view?.findViewById<TextView>(R.id.value_membership_ends)
                            
                            valuePlan?.text = membershipData?.plan ?: "N/A"
                            memberShip?.text = formatDate(membershipData?.endDate)
                            
                            Log.d("MembershipDetails", "Membership loaded successfully: ${membershipData?.plan}")
                        } else {
                            // Fallback to old API if new one fails
                            fetchMembershipDetailsFallback(it.id)
                        }
                    }

                    override fun onFailure(call: Call<MembershipUpgradeResponse>, t: Throwable) {
                        Log.e("MembershipDetails", "New API failed: ${t.message}")
                        // Fallback to old API
                        fetchMembershipDetailsFallback(it.id)
                    }
                })
        }
    }

    private fun fetchMembershipDetailsFallback(userId: Int) {
        RetrofitClient.members.getMemberShip(userId)
            .enqueue(object : Callback<MembershipPlan> {
                override fun onResponse(
                    call: Call<MembershipPlan>,
                    response: Response<MembershipPlan>
                ) {
                    if (response.isSuccessful) {
                        val subscription = response.body()
                        val valuePlan = view?.findViewById<TextView>(R.id.value_plan)
                        val memberShip = view?.findViewById<TextView>(R.id.value_membership_ends)
                        valuePlan?.text = subscription?.plan ?: "N/A"
                        memberShip?.text = formatDate(subscription?.startDate)
                        Log.d("MembershipDetails", "Fallback API loaded successfully")
                    } else {
                        Log.e("MembershipDetails", "Both APIs failed: ${response.code()}")
                        showErrorState()
                    }
                }

                override fun onFailure(call: Call<MembershipPlan>, t: Throwable) {
                    Log.e("MembershipDetails", "Fallback API also failed: ${t.message}")
                    showErrorState()
                }
            })
    }

    private fun showErrorState() {
        val valuePlan = view?.findViewById<TextView>(R.id.value_plan)
        val memberShip = view?.findViewById<TextView>(R.id.value_membership_ends)
        valuePlan?.text = "Unable to load"
        memberShip?.text = "Unable to load"
    }
}
