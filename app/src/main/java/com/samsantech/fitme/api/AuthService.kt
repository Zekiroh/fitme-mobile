package com.samsantech.fitme.api

import com.samsantech.fitme.model.LoginRequest
import com.samsantech.fitme.model.LoginResponse
import com.samsantech.fitme.model.RegUpdateParam
import com.samsantech.fitme.model.RegisterRequest
import com.samsantech.fitme.model.RegisterResponse
import com.samsantech.fitme.model.ResponseSuccess
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthService {
    // Mobile login (members only)
    @POST("auth/login-member")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    // Register new mobile user
    @Headers("Content-Type: application/json")
    @POST("auth/register-member")
    fun registerUser(@Body request: RegisterRequest): Call<RegisterResponse>

    @Headers("Content-Type: application/json")
    @PATCH("users/{id}")
     fun updateFitReg(@Path("id") userId: Int, @Body body: RegUpdateParam):
    Call<ResponseSuccess>

    // 🔥 Forgot password for mobile users
    @POST("auth/forgot-password")
    fun sendResetEmail(@Query("email") email: String): Call<ResponseBody>
}
