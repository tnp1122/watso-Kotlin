package com.watso.app

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.watso.app.util.ActivityController
import com.watso.app.util.ErrorString
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

    fun onError(msg: String, errMsg: String?, TAG: String?) {
        hideProgressBar()

        if (errMsg == null) {
            showToast("$msg: ${ErrorString.E5001}")
            return
        }

        Log.e(TAG, "onError: $msg")
        showToast(msg)
    }

    fun onException(msg: String, exMsg: String?, TAG: String?) {
        hideProgressBar()

        if (exMsg == null) {
            showToast("$msg: ${ErrorString.E5000}")
            return
        }

        Log.e(TAG, "$msg: $exMsg")
        showToast("$msg: $exMsg")
    }

    fun onBackPressed() {
        AC.onBackPressed()
    }

    fun navigateTo(fragment: Fragment, popBackStack:Int = -1) {
        AC.navigateTo(fragment, popBackStack = popBackStack)
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
}
