package com.watso.app.feature.auth.ui.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
//import com.watso.app.FragmentHome
import com.watso.app.MainActivity
import com.watso.app.data.model.BaseResponse
import com.watso.app.databinding.FragLoginBinding
import com.watso.app.feature.auth.ui.viewModel.LoginViewModel
import com.watso.app.feature.user.ui.view.FragmentSignUp
import com.watso.app.fragmentAccount.FragmentFindAccount
import com.watso.app.util.ActivityController
import com.watso.app.util.ErrorString

private const val LOGIN_FAIL = "로그인 실패"

class FragmentLogin :Fragment() {

    private lateinit var binding: FragLoginBinding
    private lateinit var AC: ActivityController
    private val loginViewModel by viewModels<LoginViewModel>()
    private val TAG = "[FragLogin]"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        binding = FragLoginBinding.inflate(layoutInflater)

        val mActivity = activity as MainActivity
        AC = ActivityController(mActivity)

        setClickListeners()
        setObservers(mActivity)

        return binding.root
    }

    private fun setClickListeners() {
        binding.btnLogin.setOnClickListener { doLogin() }
        binding.tvFindAccount.setOnClickListener { navigateTo(FragmentFindAccount()) }
        binding.btnSignup.setOnClickListener { navigateTo(FragmentSignUp()) }
    }

    private fun setObservers(mActivity: MainActivity) {
        loginViewModel.loginResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onSuccess()
                is BaseResponse.Error -> onError(LOGIN_FAIL, it.msg)
                else -> onException(LOGIN_FAIL, it.toString())
            }
        }
    }

    private fun onLoading() {
        showProgressBar()
    }

    private fun onSuccess() {
        hideProgressBar()
        Log.d(TAG, "========== 로그인 성공 ==========")
        // getUserInfo 과정 구현하기
//        navigateTo(FragmentHome(), 0)
    }

    fun onError(msg: String, errMsg: String?) {
        hideProgressBar()

        if (errMsg == null) {
            showToast("$msg: ${ErrorString.E5001}")
            return
        }

        Log.e(TAG, "onError: $msg")
        showToast(msg)
    }

    fun onException(msg: String, exMsg: String?) {
        hideProgressBar()

        if (exMsg == null) {
            showToast("$msg: ${ErrorString.E5000}")
            return
        }

        Log.e(TAG, "$msg: $exMsg")
        showToast("$msg: $exMsg")
    }

    private fun navigateTo(fragment: Fragment, popBackStack:Int = -1) {
        AC.navigateTo(fragment, popBackStack = popBackStack)
    }

    private fun doLogin() {
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()
        loginViewModel.login(username, password)
    }

    private fun showProgressBar() {
        AC.showProgressBar()
    }

    private fun hideProgressBar() {
        AC.hideProgressBar()
    }

    fun showToast(msg: String) {
        AC.showToast(msg)
    }
}
