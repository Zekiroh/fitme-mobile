package com.samsantech.fitme.api

import com.samsantech.fitme.model.PaymentData
import com.samsantech.fitme.model.PaymentRequest
import com.samsantech.fitme.model.PaymentRequestCallback
import com.samsantech.fitme.model.PaymentResponse
import com.samsantech.fitme.model.PaymentStatusResponse
import com.samsantech.fitme.model.PlanResponse
import com.samsantech.fitme.model.MembershipUpgradeRequest
import com.samsantech.fitme.model.MembershipUpgradeResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface Payments {
    @POST("payment/payment-checkout")
    fun paymentCheckouts(@Body request: PaymentRequest): Call<PaymentResponse>

    @POST("payment/payment-callback/{userid}")
    fun paymentCallBack(@Path("userid") userId: Int, @Body request: PaymentRequestCallback): Call<PaymentStatusResponse>

    @GET("payment/get-plan")
    fun getPlans(): Call<PlanResponse>

    @POST("payment/upgrade-membership")
    fun upgradeMembership(@Body request: MembershipUpgradeRequest): Call<MembershipUpgradeResponse>

    @GET("payment/membership/{user_id}")
    fun getCurrentMembership(@Path("user_id") userId: Int): Call<MembershipUpgradeResponse>
}