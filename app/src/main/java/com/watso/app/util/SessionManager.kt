package com.watso.app.util

import android.content.Context
import android.content.SharedPreferences
import com.watso.app.R
import com.watso.app.feature.user.data.UserInfo

object SessionManager {

    private const val ACCESS_TOKEN = "accessToken"
    private const val REFRESH_TOKEN = "refreshToken"

    fun saveAccessToken(context: Context, token: String) {
        saveString(context, ACCESS_TOKEN, token)
    }

    fun saveRefreshToken(context: Context, token: String) {
        saveString(context, REFRESH_TOKEN, token)
    }

    fun saveUserInfo(context: Context, userInfo: UserInfo) {
        saveLong(context, "userId", userInfo._id)
        saveString(context, "name", userInfo.name)
        saveString(context, "username", userInfo.username)
        saveString(context, "nickname", userInfo.nickname)
        saveString(context, "accountNumber", userInfo.accountNumber)
        saveString(context, "email", userInfo.email)
    }

    fun getAccessToken(context: Context): String {
        return getString(context, ACCESS_TOKEN)
    }

    fun getRefreshToken(context: Context): String {
        return getString(context, REFRESH_TOKEN)
    }

    fun getUserInfo(context: Context): UserInfo {
        val _id = getLong(context, "userId")
        val name = getString(context, "name")
        val username = getString(context, "username")
        val nickname = getString(context, "nickname")
        val accountNumber = getString(context, "accountNumber")
        val email = getString(context, "email")

        return UserInfo(_id, name, username, nickname, accountNumber, email)
    }

    fun getUserId(context: Context): Long {
        return getUserInfo(context)._id
    }

    fun getName(context: Context): String {
        return getUserInfo(context).name
    }

    fun getNickname(context: Context): String {
        return getUserInfo(context).nickname
    }

    fun getAccountNumber(context: Context): String {
        return getUserInfo(context).accountNumber
    }

    private fun saveString(context: Context, key: String, value: String) {
        val prefs = PreferenceUtil(context)
        prefs.setString(key, value)
    }

    private fun saveLong(context: Context, key: String, value: Long) {
        val prefs = PreferenceUtil(context)
        prefs.setLong(key, value)
    }

    private fun getString(context: Context, key: String): String {
        val prefs: SharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        return prefs.getString(key, "").toString()
    }

    private fun getLong(context: Context, key: String): Long {
        val prefs: SharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        return prefs.getLong(key, -1)
    }
}
