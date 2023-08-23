package com.watso.app

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.watso.app.util.ActivityController
import com.watso.app.util.ErrorString
import com.watso.app.util.VerifyInput
import okhttp3.ResponseBody
import java.lang.RuntimeException

data class ErrorResponse(val msg: String, val code: Int)

open class BaseFragment: Fragment() {

    lateinit var AC: ActivityController
    lateinit var mActivity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            this.mActivity = context
            AC = ActivityController(mActivity)
        } else {
            throw RuntimeException("$context must be MainActivity")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideSoftInput()
    }

    fun onLoading() {
        showProgressBar()
    }

    fun onSuccess() {
        hideProgressBar()
    }

    /**
     * TAG: tag
     * method: 메서드 명
     * errorBody: response body
     * msg: http message
     */
    fun onError(TAG: String, method: String, errorBody: ResponseBody?, msg: String?) {
        hideProgressBar()

        if (errorBody != null) {
            val gson = Gson()
            val errorBodyObject = gson.fromJson(errorBody.string(), ErrorResponse::class.java)
            Log.e(TAG,"$method ${ErrorString.FAIL} (${errorBodyObject.msg} - ${errorBodyObject.code})")
            showToast(errorBodyObject.msg)
            return
        }

        if (msg == null) {
            val errMsg = "$method ${ErrorString.FAIL}: ${ErrorString.E5001}"
            Log.e(TAG, errMsg)
            showToast(errMsg)
            return
        }

        val errMsg = "$method ${ErrorString.FAIL} [$msg]"
        Log.e(TAG, errMsg)
        showToast(errMsg)
    }

    fun onException(TAG: String, method: String, exMsg: String) {
        hideProgressBar()

        Log.e(TAG, "$method ${ErrorString.FAIL}, $exMsg")
        showToast("$method ${ErrorString.FAIL}")
    }

    fun onExceptionalProblem(TAG: String, method: String) {
        val errMsg = "$method ${ErrorString.FAIL}: ${ErrorString.E5002}"
        Log.e(TAG, errMsg)
        showToast(errMsg)
    }

    fun onBackPressed() {
        AC.onBackPressed()
    }

    fun navigateTo(fragment: Fragment, arguments: Map<String, String>? = null, popBackStack:Int = -1, fragIndex:Int = 1) {
        AC.navigateTo(fragment, arguments, popBackStack, fragIndex)
    }

    fun showSoftInput(view: View) {
        AC.showSoftInput(view)
    }

    fun hideSoftInput() {
        AC.hideSoftInput()
    }

    fun showProgressBar() {
        AC.showProgressBar()
    }

    fun hideProgressBar() {
        AC.hideProgressBar()
    }

    fun showToast(msg: String) {
        AC.showToast(msg)
    }

    fun showAlert(msg: String) {
        AC.showAlert(msg)
    }

    fun verifyInput(case: String, text: String): Boolean {
        val message = VerifyInput.verifyInput(case, text)
        return if (message == "") {
            true
        } else {
            val builder = AlertDialog.Builder(mActivity)
            builder.setMessage(message)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> })
                .show()
            false
        }
    }
}
