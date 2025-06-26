package com.samsantech.fitme.api

import com.samsantech.fitme.model.LoginRequest
import com.samsantech.fitme.model.LoginResponse
import com.samsantech.fitme.model.RegisterRequest
import com.samsantech.fitme.model.RegisterResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    // Mobile login (members only)
    @POST("api/auth/login-member")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    // Register new mobile user
    @POST("api/auth/register-member")
    fun registerUser(@Body request: RegisterRequest): Call<RegisterResponse>
}
