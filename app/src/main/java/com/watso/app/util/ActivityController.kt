package com.watso.app.util

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View
import androidx.fragment.app.Fragment
import com.watso.app.MainActivity

class ActivityController(private val activity: MainActivity) {
    private var progressStack = 0
    private val prefs = MainActivity.prefs

    fun navigateTo(fragment: Fragment, arguments: Map<String, String>? = null, popBackStack:Int = -1, fragIndex:Int = 1) {
        hideSoftInput()
        activity.navigateTo(fragment, arguments, popBackStack, fragIndex)
    }

    fun onBackPressed() {
        hideSoftInput()
        activity.onBackPressed()
    }

    fun showProgressBar() {
        if (progressStack == 0) {
            activity.showProgress()
        }
        progressStack += 1
    }

    fun hideProgressBar() {
        progressStack -= 1
        if (progressStack == 0) {
            activity.hideProgress()
        }
    }

    fun setString(key: String, value: String) {
        prefs.setString(key, value)
    }

    fun getString(key: String, defValue: String = ""): String {
        return prefs.getString(key, defValue)
    }

    fun removeString(key: String) {
        prefs.removeString(key)
    }

    fun makeToast(message: String){
        activity.makeToast(message)
    }

    fun showToast(message: String) {
        activity.showToast(message)
    }

    fun showSoftInput(view: View) {
        view.requestFocus()
        activity.showSoftInput(view)
    }

    fun hideSoftInput() {
        activity.hideSoftInput()
    }

    fun showAlert(msg: String, title: String? = null) {
        AlertDialog.Builder(activity).setTitle(title)
            .setMessage(msg)
            .setPositiveButton("확인", DialogInterface.OnClickListener { _, _ -> })
            .show()
    }

    fun copyToClipboard(label:String="", content: String) {
        activity.copyToClipboard(label, content)
    }

    fun decodeToken(jwt: String): String {
        return activity.decodeToken(jwt)
    }

    fun logOut(message: String?=null) {
        activity.logOut(message)
    }
}
