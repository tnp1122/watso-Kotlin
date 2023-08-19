package com.watso.app.feature.auth.ui.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.watso.app.BaseFragment
import com.watso.app.MainActivity
import com.watso.app.data.model.BaseResponse
import com.watso.app.databinding.FragLoginBinding
import com.watso.app.feature.auth.ui.viewModel.LoginViewModel
import com.watso.app.feature.user.ui.view.FragmentSignup
import com.watso.app.fragmentAccount.FragmentFindAccount

private const val LOGIN_FAIL = "로그인 실패"

class FragmentLogin: BaseFragment() {

    var mBinding: FragLoginBinding? = null
    val binding get() = mBinding!!
    val TAG = "[FragLogin]"
    val loginViewModel by viewModels<LoginViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragLoginBinding.inflate(layoutInflater)

        setClickListeners()
        setObservers()

        return binding.root
    }

    fun setClickListeners() {
        binding.btnLogin.setOnClickListener { doLogin() }
        binding.tvFindAccount.setOnClickListener { navigateTo(FragmentFindAccount()) }
        binding.btnSignup.setOnClickListener { navigateTo(FragmentSignup()) }
    }

    fun setObservers() {
        loginViewModel.loginResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onSuccess()
                is BaseResponse.Error -> onError(LOGIN_FAIL, it.msg, TAG)
                else -> onException(LOGIN_FAIL, it.toString(), TAG)
            }
        }
    }

    fun onSuccess() {
        hideProgressBar()
        Log.d(TAG, "========== 로그인 성공 ==========")
        // getUserInfo 과정 구현하기
//        navigateTo(FragmentHome(), 0)
    }

    fun doLogin() {
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()
        loginViewModel.login(username, password)
    }
}
