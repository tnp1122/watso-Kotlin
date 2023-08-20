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
import com.watso.app.util.ActivityController
import com.watso.app.util.ErrorString
import com.watso.app.util.VerifyInput
import java.lang.RuntimeException

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
    }

    fun onLoading() {
        showProgressBar()
    }

    fun onSuccess() {
        hideProgressBar()
    }

    fun onError(msg: String, errMsg: String?, TAG: String) {
        hideProgressBar()

        if (errMsg == null) {
            showToast("$msg: ${ErrorString.E5001}")
            return
        }

        Log.e(TAG, "onError: $msg")
        showToast(msg)
    }

    fun onException(msg: String, exMsg: String?, TAG: String) {
        hideProgressBar()

        if (exMsg == null) {
            showToast("$msg: ${ErrorString.E5000}")
            return
        }

        Log.e(TAG, "$msg: $exMsg")
        showToast("$msg: $exMsg")
    }

    fun onExceptionalProblem(TAG: String) {
        Log.e(TAG, ErrorString.E5002)
        showToast(ErrorString.E5002)
    }

    fun onBackPressed() {
        AC.onBackPressed()
    }

    fun navigateTo(fragment: Fragment, arguments: Map<String, String>? = null, popBackStack:Int = -1, fragIndex:Int = 1) {
        AC.navigateTo(fragment, arguments, popBackStack, fragIndex)
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
