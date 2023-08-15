package com.watso.app.util

import android.content.Context
import android.content.SharedPreferences
import com.watso.app.R

class PreferenceUtil(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
    private val editor = prefs.edit()

    fun getString(key: String, defValue: String): String {
        return prefs.getString(key, defValue).toString()
    }

    fun setString(key: String, str: String) {
        editor.putString(key, str).apply()
        editor.commit()
    }

    fun removeString(key: String){
        editor.remove(key)
        editor.commit()
    }

    fun clearData() {
        editor.clear()
        editor.apply()
    }
}
