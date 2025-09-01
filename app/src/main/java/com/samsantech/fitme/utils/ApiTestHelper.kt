package com.samsantech.fitme.utils

import android.util.Log
import com.samsantech.fitme.api.RetrofitClient
import com.samsantech.fitme.model.MembershipUpgradeRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ApiTestHelper {
    
    private const val TAG = "API_TEST"
    
    /**
     * Test the membership upgrade API with sample data
     */
    fun testMembershipUpgradeAPI(userId: Int) {
        Log.d(TAG, "Testing membership upgrade API for user: $userId")
        
        val testRequest = MembershipUpgradeRequest(
            userId = userId,
            plan = "Pro Plan",
            price = 1500,
            paymentMethod = "GCASH",
            gcashNumber = "09123456789",
            gcashName = "Test User"
        )
        
        RetrofitClient.payments.upgradeMembership(testRequest)
            .enqueue(object : Callback<com.samsantech.fitme.model.MembershipUpgradeResponse> {
                override fun onResponse(
                    call: Call<com.samsantech.fitme.model.MembershipUpgradeResponse>,
                    response: Response<com.samsantech.fitme.model.MembershipUpgradeResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "✅ API Test Success: ${response.body()?.message}")
                        Log.d(TAG, "Response data: ${response.body()?.data}")
                    } else {
                        Log.e(TAG, "❌ API Test Failed: ${response.code()}")
                        Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
                    }
                }
                
                override fun onFailure(
                    call: Call<com.samsantech.fitme.model.MembershipUpgradeResponse>,
                    t: Throwable
                ) {
                    Log.e(TAG, "❌ API Test Network Error: ${t.message}")
                }
            })
    }
    
    /**
     * Test the get current membership API
     */
    fun testGetCurrentMembershipAPI(userId: Int) {
        Log.d(TAG, "Testing get current membership API for user: $userId")
        
        RetrofitClient.payments.getCurrentMembership(userId)
            .enqueue(object : Callback<com.samsantech.fitme.model.MembershipUpgradeResponse> {
                override fun onResponse(
                    call: Call<com.samsantech.fitme.model.MembershipUpgradeResponse>,
                    response: Response<com.samsantech.fitme.model.MembershipUpgradeResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "✅ Get Membership API Success: ${response.body()?.message}")
                        Log.d(TAG, "Current membership: ${response.body()?.data}")
                    } else {
                        Log.e(TAG, "❌ Get Membership API Failed: ${response.code()}")
                        Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
                    }
                }
                
                override fun onFailure(
                    call: Call<com.samsantech.fitme.model.MembershipUpgradeResponse>,
                    t: Throwable
                ) {
                    Log.e(TAG, "❌ Get Membership API Network Error: ${t.message}")
                }
            })
    }
    
    /**
     * Test the get plans API
     */
    fun testGetPlansAPI() {
        Log.d(TAG, "Testing get plans API")
        
        RetrofitClient.payments.getPlans()
            .enqueue(object : Callback<com.samsantech.fitme.model.PlanResponse> {
                override fun onResponse(
                    call: Call<com.samsantech.fitme.model.PlanResponse>,
                    response: Response<com.samsantech.fitme.model.PlanResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "✅ Get Plans API Success")
                        Log.d(TAG, "Available plans: ${response.body()?.plans?.size} plans found")
                        response.body()?.plans?.forEach { plan ->
                            Log.d(TAG, "Plan: ${plan.plan} - ₱${plan.price} (${plan.monthsCount} months)")
                        }
                    } else {
                        Log.e(TAG, "❌ Get Plans API Failed: ${response.code()}")
                        Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
                    }
                }
                
                override fun onFailure(
                    call: Call<com.samsantech.fitme.model.PlanResponse>,
                    t: Throwable
                ) {
                    Log.e(TAG, "❌ Get Plans API Network Error: ${t.message}")
                }
            })
    }
}
