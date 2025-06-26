package com.samsantech.fitme.api

import com.samsantech.fitme.model.AssistanceItem
import com.samsantech.fitme.model.AssistanceReplyRequest
import com.samsantech.fitme.model.AssistanceRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AssistanceService {
    @POST("/api/assistance")
    fun submitAssistance(@Body request: AssistanceRequest): Call<ResponseBody>

    @GET("/api/assistance/user/{id}")
    fun getUserAssistance(@Path("id") userId: Int): Call<List<AssistanceItem>>

    @POST("/api/assistance/reply")
    fun sendReply(@Body request: AssistanceReplyRequest): Call<ResponseBody>
}