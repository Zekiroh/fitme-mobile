package com.samsantech.fitme.model

import com.google.gson.annotations.SerializedName

data class PaymentRequest(
    val amount: Int,
    @SerializedName("user_id")
    val userId: Int,
    val description: String,
    val plan: String
)

data class PaymentResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("reference_number")
    val referenceNumber: String,
    @SerializedName("checkout_url")
    val checkoutUrl: String
)

data class PaymentRequestCallback(
    @SerializedName("reference_number")
    val referenceNumber: String
)


data class PaymentStatusResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: PaymentData,
    @SerializedName("message") val message: String
)

data class PaymentData(
    @SerializedName("id") val id: Int,
    @SerializedName("paid_at") val paidAt: String,
    @SerializedName("reference_number") val referenceNumber: String,
    @SerializedName("payment_method_used") val paymentMethodUsed: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String
)

data class PaymentErrorResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

data class PlanResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("plans")
    val plans: List<Plan>
)

data class Plan(
    @SerializedName("id")
    val id: Int,
    @SerializedName("plan")
    val plan: String,
    @SerializedName("price")
    val price: Int,
    @SerializedName("months_count")
    val monthsCount: Int,
    @SerializedName("is_active")
    val isActive: Int
)