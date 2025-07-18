package com.samsantech.fitme.components

import android.content.Context
import com.google.gson.Gson
import com.samsantech.fitme.model.User


object SharedPrefHelper {

    private const val PREF_NAME = "usersInfo"
    private const val USER_DATA_KEY = "user_data"

    fun getLoggedInUser(context: Context?): User? {
        val sharedPref = context?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val userJson = sharedPref?.getString(USER_DATA_KEY, null)
        return if (!userJson.isNullOrEmpty()) {
            Gson().fromJson(userJson, User::class.java)
        } else null
    }
}