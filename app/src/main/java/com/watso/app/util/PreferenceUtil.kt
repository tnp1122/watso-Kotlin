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

    fun getInt(key: String, defValue: Int): Int {
        return prefs.getInt(key, defValue)
    }

    fun getLong(key: String, defValue: Long): Long {
        return prefs.getLong(key, defValue)
    }

    fun getFloat(key: String, defValue: Float): Float {
        return prefs.getFloat(key, defValue)
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return prefs.getBoolean(key, defValue)
    }

    fun setString(key: String, str: String) {
        editor.putString(key, str).apply()
        editor.commit()
    }

    fun setInt(key: String, value:Int) {
        editor.putInt(key, value).apply()
        editor.commit()
    }

    fun setLong(key: String, value: Long) {
        editor.putLong(key, value).apply()
        editor.commit()
    }

    fun setFloat(key: String, value:Float) {
        editor.putFloat(key, value).apply()
        editor.commit()
    }

    fun setBoolean(key: String, value:Boolean) {
        editor.putBoolean(key, value).apply()
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
