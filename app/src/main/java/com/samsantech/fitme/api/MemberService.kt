package com.samsantech.fitme.api

import android.telecom.Call
import com.samsantech.fitme.model.ResponseSuccess
import retrofit2.http.DELETE
import retrofit2.http.Path

interface MemberService {
    @DELETE("members/del/{userid}")
    fun deleteMembersAccount(@Path("userid") userId: Int): retrofit2.Call<ResponseSuccess>
}