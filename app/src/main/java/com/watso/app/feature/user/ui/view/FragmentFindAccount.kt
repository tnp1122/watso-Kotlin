package com.watso.app.feature.user.ui.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.watso.app.BaseFragment
import com.watso.app.R
import com.watso.app.data.model.BaseResponse
import com.watso.app.databinding.FragFindAccountBinding
import com.watso.app.feature.user.data.TempPasswordForm
import com.watso.app.feature.user.ui.viewModel.FindAccountViewModel

private const val FIND_USERNAME = "아이디 찾기"
private const val ISSUE_TEMP_PASSWORD = "임시 비밀번호 발급"

class FragmentFindAccount :BaseFragment() {

    var mBinding: FragFindAccountBinding? = null
    val binding get() = mBinding!!
    val TAG = "[FragFindAccount]"
    val findAccountViewModel by viewModels<FindAccountViewModel>()

    var forgot = "username"
    var isSendAble = true       // 중복 클릭 방지 flag

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragFindAccountBinding.inflate(inflater, container, false)

        setUpUI()
        setClickListeners()
        setObservers()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }

    fun setUpUI() {
        binding.lytFindPassword.visibility = View.GONE
    }

    fun setClickListeners() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }
        binding.tvFindUsername.setOnClickListener {
            forgot = "username"
            isSendAble = true
            hideSoftInput()
            binding.etEmailPassword.setText("")
            binding.etUsername.setText("")
            binding.lytFindUsername.visibility = View.VISIBLE
            binding.lytFindPassword.visibility = View.GONE
            binding.tvFindUsername.setTextColor(ContextCompat.getColor(fragmentContext, R.color.primary))
            binding.tvFindPassword.setTextColor(ContextCompat.getColor(fragmentContext, R.color.black))
        }

        binding.tvFindPassword.setOnClickListener {
            forgot = "password"
            isSendAble = true
            hideSoftInput()
            binding.etEmailUsername.setText("")
            binding.lytFindUsername.visibility = View.GONE
            binding.lytFindPassword.visibility = View.VISIBLE
            binding.tvFindUsername.setTextColor(ContextCompat.getColor(fragmentContext, R.color.black))
            binding.tvFindPassword.setTextColor(ContextCompat.getColor(fragmentContext, R.color.primary))
        }
        
        binding.btnFindUsername.setOnClickListener {
            hideSoftInput()
            val email = "${binding.etEmailUsername.text}@pusan.ac.kr"
            if (isSendAble && verifyInput("email", email)) {
                isSendAble = false
                findAccountViewModel.findUsername(email)
            }
        }

        binding.btnIssueTempPassword.setOnClickListener {
            hideSoftInput()
            val username = binding.etUsername.text.toString()
            val email = "${binding.etEmailPassword.text}@pusan.ac.kr"
            if (isSendAble && verifyInput("username", username) && verifyInput("email", email)) {
                isSendAble = false
                val tempPasswordForm = TempPasswordForm(username, email)
                findAccountViewModel.issueTempPassword(tempPasswordForm)
            }
        }
    }

    fun setObservers() {
        findAccountViewModel.findUsernameResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onFindUsernameSuccess()
                is BaseResponse.Error -> onError(TAG, FIND_USERNAME, it.errorBody, it.msg)
                else -> onException(TAG, FIND_USERNAME, it.toString())
            }
        }

        findAccountViewModel.issueTempPasswordResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onIssueTempPasswordSuccess()
                is BaseResponse.Error -> onError(TAG, ISSUE_TEMP_PASSWORD, it.errorBody, it.msg)
                else -> onException(TAG, ISSUE_TEMP_PASSWORD, it.toString())
            }
        }
    }

    fun onFindUsernameSuccess() {
        super.onSuccess()
        binding.tvResultUsername.text = "입력하신 메일로 아이디가 전송되었습니다."
    }

    fun onIssueTempPasswordSuccess() {
        super.onSuccess()
        binding.tvResultPassword.text = "입력하신 메일로 임시 비밀번호가 전송되었습니다."
    }
}
