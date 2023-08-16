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
import com.watso.app.feature.user.ui.view.FragmentSignUp
import com.watso.app.fragmentAccount.FragmentFindAccount

private const val LOGIN_FAIL = "로그인 실패"

class FragmentLogin: BaseFragment() {

    private lateinit var binding: FragLoginBinding
    private val loginViewModel by viewModels<LoginViewModel>()
    private val TAG = "[FragLogin]"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragLoginBinding.inflate(layoutInflater)

        val mActivity = activity as MainActivity

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
                is BaseResponse.Error -> onError(LOGIN_FAIL, it.msg, TAG)
                else -> onException(LOGIN_FAIL, it.toString(), TAG)
            }
        }
    }

    private fun onSuccess() {
        hideProgressBar()
        Log.d(TAG, "========== 로그인 성공 ==========")
        // getUserInfo 과정 구현하기
//        navigateTo(FragmentHome(), 0)
    }

    private fun doLogin() {
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()
        loginViewModel.login(username, password)
    }
}
