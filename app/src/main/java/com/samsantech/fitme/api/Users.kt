package com.samsantech.fitme.api

import com.samsantech.fitme.model.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface Users {
    @GET("users/{userid}")
    fun getUsers(@Path("userid") userId: Int): Call<User>
}