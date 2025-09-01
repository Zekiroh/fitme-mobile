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

class MembershipDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_membership_detailss, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchMembershipDetails()
        setupChangePlanButton()
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
                            memberShip?.text = membershipData?.endDate ?: "N/A"
                            
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
                        memberShip?.text = subscription?.startDate ?: "N/A"
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
