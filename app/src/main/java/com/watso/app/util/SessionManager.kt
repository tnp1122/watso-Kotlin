package com.watso.app.util

import android.content.Context
import android.content.SharedPreferences
import com.watso.app.R

object SessionManager {

    private const val ACCESS_TOKEN = "accessToken"
    private const val REFRESH_TOKEN = "refreshToken"

    fun saveAccessToken(context: Context, token: String) {
        setString(context, ACCESS_TOKEN, token)
    }

    fun saveRefreshToken(context: Context, token: String) {
        setString(context, REFRESH_TOKEN, token)
    }

    fun getAccessToken(context: Context): String {
        return getString(context, ACCESS_TOKEN)
    }

    fun getRefreshToken(context: Context): String {
        return getString(context, REFRESH_TOKEN)
    }

    fun setString(context: Context, key: String, value: String) {
        val prefs = PreferenceUtil(context)
        prefs.setString(key, value)
    }

    fun getString(context: Context, key: String): String {
        val prefs: SharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        return prefs.getString(key, "").toString()
    }
}
