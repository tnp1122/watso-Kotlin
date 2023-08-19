package com.watso.app.feature.auth.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.watso.app.BaseFragment
import com.watso.app.data.model.BaseResponse
import com.watso.app.databinding.FragLoginBinding
import com.watso.app.feature.auth.ui.viewModel.LoginViewModel
import com.watso.app.feature.user.ui.view.FragmentSignup
import com.watso.app.fragmentAccount.FragmentFindAccount
import com.watso.app.util.ErrorString
import okhttp3.Headers

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
        loginViewModel.loginResponseHeaders.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onSuccess(it.data)
                is BaseResponse.Error -> onError(LOGIN_FAIL, it.msg, TAG)
                else -> onException(LOGIN_FAIL, it.toString(), TAG)
            }
        }
    }

    fun onSuccess(headers: Headers?) {
        hideProgressBar()

        if (headers == null) {
            showToast(ErrorString.E5002)
            return
        }

        val token = headers["Authentication"].toString().split("/")
        mActivity.saveJWT(token)
        mActivity.getUserInfo()
    }

    fun doLogin() {
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()
        loginViewModel.login(username, password)
    }
}
