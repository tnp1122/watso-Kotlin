package com.watso.app.feature.user.ui.view

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.watso.app.BaseFragment
import com.watso.app.data.model.BaseResponse
import com.watso.app.databinding.FragUpdateAccountBinding
import com.watso.app.feature.user.data.AccountNumber
import com.watso.app.feature.user.data.CheckDuplicateResponse
import com.watso.app.feature.user.data.Nickname
import com.watso.app.feature.user.data.UpdatePasswordForm
import com.watso.app.feature.user.ui.viewModel.UpdateAccountViewModel
import com.watso.app.util.SessionManager
import okhttp3.Headers

private const val REFRESH_TOKEN_FAIL = "리프래시 토큰 갱신 실패"
private const val CHECK_NICKNAME_FAIL = "닉네임 중복체크 중 오류가 발생했습니다."
private const val UPDATE_PASSWORD_FAIL = "비밀번호 변경 실패"
private const val UPDATE_NICKNAME_FAIL = "닉네임 변경 실패"
private const val UPDATE_ACCOUNT_NUMBER_FAIL = "계좌번호 변경 실패"

class FragmentUpdateAccount :BaseFragment() {

    lateinit var fragmentContext: Context

    var mBinding: FragUpdateAccountBinding? = null
    val binding get() = mBinding!!
    val TAG = "[FragUpdateAccount]"
    val updateAccountViewModel by viewModels<UpdateAccountViewModel> ()

    var target = ""
    var checkedNickname = ""
    var checkedPassword = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            target = it.getString("target")!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragUpdateAccountBinding.inflate(inflater, container, false)

        binding.btnPrevious.setOnClickListener { AC.onBackPressed() }

        when (target) {
            "password" -> setLytPassword()
            "nickname" -> setLytNickname()
            else -> setLytAccountNum()
        }

        setObservers()
        return binding.root
    }

    fun setObservers() {
        updateAccountViewModel.refreshTokenHeaders.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onRefreshTokenSuccess(it.data)
                is BaseResponse.Error -> onError(REFRESH_TOKEN_FAIL, it.msg, TAG)
                else -> onException(REFRESH_TOKEN_FAIL, it.toString(), TAG)
            }
        }

        updateAccountViewModel.checkNicknameResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onCheckNicknameSuccess(it.data)
                is BaseResponse.Error -> onError(CHECK_NICKNAME_FAIL, it.msg, TAG)
                else -> onException(CHECK_NICKNAME_FAIL, it.toString(), TAG)
            }
        }

        updateAccountViewModel.updatePasswordResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onUpdatePasswordSuccess()
                is BaseResponse.Error -> onError(UPDATE_PASSWORD_FAIL, it.msg, TAG)
                else -> onException(UPDATE_PASSWORD_FAIL, it.toString(), TAG)
            }
        }

        updateAccountViewModel.updateNicknameResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onUpdateNicknameSuccess()
                is BaseResponse.Error -> onError(UPDATE_NICKNAME_FAIL, it.msg, TAG)
                else -> onException(UPDATE_NICKNAME_FAIL, it.toString(), TAG)
            }
        }

        updateAccountViewModel.updateAccountNumberResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onUpdateAccountNumberSuccess()
                is BaseResponse.Error -> onError(UPDATE_ACCOUNT_NUMBER_FAIL, it.msg, TAG)
                else -> onException(UPDATE_ACCOUNT_NUMBER_FAIL, it.toString(), TAG)
            }
        }
    }

    fun onRefreshTokenSuccess(headers: Headers?) {
        super.onSuccess()

        if (headers == null) {
            onExceptionalProblem(TAG)
            return
        }

        val token = headers["Authentication"]
        if (token == null) {
            onExceptionalProblem(TAG)
            return
        }

        SessionManager.saveAccessToken(mActivity, token)
        getUserInfo()
    }

    fun onCheckNicknameSuccess(data: CheckDuplicateResponse?) {
        super.onSuccess()

        if (data == null) {
            onExceptionalProblem(TAG)
            return
        }

        if (data.isDuplicated) {
            binding.tvNicknameConfirm.text = "사용 불가능한 닉네임입니다."
            binding.tvNicknameConfirm.setTextColor(Color.RED)
        } else {
            binding.tvNicknameConfirm.text = "사용 가능한 닉네임입니다."
            binding.tvNicknameConfirm.setTextColor(Color.BLACK)
            checkedNickname = binding.etNickname.text.toString()
        }
    }

    fun onUpdatePasswordSuccess() {
        super.onSuccess()
        showToast("비밀번호가 변경되었습니다.")
        onBackPressed()
    }

    fun onUpdateNicknameSuccess() {
        super.onSuccess()
        showToast("닉네임이 변경되었습니다.")
        refreshToken()
        onBackPressed()
    }

    fun onUpdateAccountNumberSuccess() {
        super.onSuccess()
        showToast("계좌번호가 변경되었습니다.")
        getUserInfo()
        onBackPressed()
    }

    fun onChangedPassword() {
        if (binding.etNewPassword.text.toString() != "" && binding.etPasswordConfirm.text.toString() != "") {
            if (binding.etNewPassword.text.toString().equals(binding.etPasswordConfirm.text.toString())) {
                binding.tvPasswordConfirm.text = "비밀번호가 일치합니다."
                binding.tvPasswordConfirm.setTextColor(Color.BLACK)
                checkedPassword = binding.etNewPassword.text.toString()
            } else {
                binding.tvPasswordConfirm.text = "비밀번호가 일치하지 않습니다."
                binding.tvPasswordConfirm.setTextColor(Color.RED)
                checkedPassword = ""
            }
        } else {
            binding.tvPasswordConfirm.text = ""
            checkedPassword = ""
        }
    }

    fun setLytPassword() {
        binding.tvTitle.text = "비밀번호 변경"
        binding.lytAccountNum.visibility = View.GONE
        binding.lytNickname.visibility = View.GONE

        binding.etPasswordConfirm.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) { onChangedPassword() }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
        binding.etNewPassword.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) { onChangedPassword() }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        binding.btnUpdatePassword.setOnClickListener {
            val currentPassword = binding.etCurrentPassword.text.toString()
            if (currentPassword != "" && checkedPassword != "") {
                if (verifyInput("password", checkedPassword)) {
                    val builder = AlertDialog.Builder(fragmentContext)
                    builder.setTitle("비밀번호 변경")
                        .setMessage("비밀번호를 변경하시겠습니까?")
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                            updatePassword(currentPassword) })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
                    builder.show()
                }
            }
        }
    }

    fun setLytNickname() {
        binding.tvTitle.text = "닉네임 변경"
        binding.lytAccountNum.visibility = View.GONE
        binding.lytPassword.visibility = View.GONE

        binding.etNickname.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (checkedNickname != "" && binding.etNickname.text.toString() == checkedNickname) {
                    binding.tvNicknameConfirm.text = "사용 가능한 닉네임입니다."
                    binding.tvNicknameConfirm.setTextColor(Color.BLACK)
                } else {
                    binding.tvNicknameConfirm.text = "닉네임 중복확인이 필요합니다."
                    binding.tvNicknameConfirm.setTextColor(Color.RED)
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        binding.btnNicknameDuplicationCheck.setOnClickListener {
            if (verifyInput("nickname", binding.etNickname.text.toString())) {
                updateAccountViewModel.checkNickname(binding.etNickname.text.toString())
            }
        }
        binding.btnUpdateNickname.setOnClickListener {
            if (checkedNickname != "" && binding.etNickname.text.toString() == checkedNickname) {
                val builder = AlertDialog.Builder(fragmentContext)
                builder.setTitle("닉네임 변경")
                    .setMessage("닉네임을 변경하시겠습니까?")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                        updateNickname(checkedNickname) })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
                builder.show()
            }
        }
    }

    fun setLytAccountNum() {
        binding.tvTitle.text = "계좌 정보 변경"
        binding.lytNickname.visibility = View.GONE
        binding.lytPassword.visibility = View.GONE

        /*val banks = resources.getStringArray(R.array.banks)
        var bankName = banks[0]

        binding.spnAccountNum.adapter = ArrayAdapter.createFromResource(
            fragmentContext, R.array.banks, android.R.layout.simple_spinner_item)
        binding.spnAccountNum.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                bankName = (banks[position])
            }
        }*/

        binding.btnUpdateAccountNum.setOnClickListener {
            if (verifyInput("accountNum", binding.etAccountNum.text.toString())) {
                val builder = AlertDialog.Builder(fragmentContext)
                builder.setTitle("계좌번호 변경")
                    .setMessage("계좌번호를 변경하시겠습니까?")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                        updateAccountNum() })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
                builder.show()
            }
        }
    }

    fun updatePassword(currentPassword: String) {
        val updatePasswordForm = UpdatePasswordForm(currentPassword, checkedPassword)
        updateAccountViewModel.updatePassword(updatePasswordForm)
    }

    fun updateNickname(checkedNickname: String) {
        updateAccountViewModel.updateNickname(Nickname(checkedNickname))
    }

    fun updateAccountNum() {
        val accountNumber = AccountNumber(binding.etAccountNum.text.toString())
        updateAccountViewModel.updateAccountNumber(accountNumber)
    }

    fun refreshToken() {
        updateAccountViewModel.refreshToken()
    }

    fun getUserInfo() {
        mActivity.getUserInfo(false)
    }
}
