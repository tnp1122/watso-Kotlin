package com.watso.app.feature.user.ui.view

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.watso.app.BaseFragment
import kotlinx.coroutines.*
import com.watso.app.R
import com.watso.app.data.model.BaseResponse
import com.watso.app.databinding.FragSignupBinding
import com.watso.app.feature.user.data.CheckDuplicateResponse
import com.watso.app.feature.user.data.SignupForm
import com.watso.app.feature.user.data.VerificationResponse
import com.watso.app.feature.user.ui.viewModel.SignupViewModel

private const val CHECK_NICKNAME = "닉네임 중복검사"
private const val CHECK_USERNAME = "아이디 중복검사"
private const val SEND_CODE = "인증코드 전송"
private const val CHECK_CODE = "인증코드 확인"
private const val SIGNUP = "회원가입"

class FragmentSignup : BaseFragment() {

    lateinit var fragmentContext: Context
    lateinit var job: Job

    var mBinding: FragSignupBinding? = null
    val binding get() = mBinding!!
    val TAG = "[FragSignup]"
    val signupViewModel by viewModels<SignupViewModel>()

    val signupCheck = mutableMapOf(
        "realName" to false,    // verifyInputFormat
        "username" to false,    // 중복확인할 때 유효성 검사 함
        "password" to false,    // verifyInputFormat, 비밀번호 확인
        "nickname" to false,    // 중복확인할 때 유효성 검사 함
        "accountNum" to false,  // verifyInputFormat
        "email" to false        // 코드확인할 때 유효성 검사 함
    )
    var remainingSeconds = 0
    var valifyTime = 300
    var sendCoolTime = 10
    var isSendAble = true
    var checkedUsername = ""
    var checkedNickname = ""
    var verifyingEmail = ""
    var verifiedEmail = ""
    var authToken = ""
    var bankName = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = FragSignupBinding.inflate(layoutInflater)

        setUpUI()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        if (::job.isInitialized && job.isActive)
            job.cancel()
    }

    fun setUpUI() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }
        binding.btnSignup.setEnabled(false)
        setUpNickname()
        setUpUsername()
        setUpPassword()
        setUpAccountNum()
        setUpEmail()
        setUpSignup()
    }

    fun setUpNickname() {
        var nickname = ""
        var verifiyingNickname = ""

        binding.etNickname.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                nickname = binding.etNickname.text.toString()
                if (checkedNickname != "" && nickname == checkedNickname) {
                    binding.tvNicknameConfirm.text = "사용 가능한 닉네임입니다."
                    binding.tvNicknameConfirm.setTextColor(Color.BLACK)
                    signupCheck["nickname"] = true
                } else {
                    binding.tvNicknameConfirm.text = "닉네임 중복확인이 필요합니다."
                    binding.tvNicknameConfirm.setTextColor(Color.RED)
                    signupCheck["nickname"] = false
                }
                if (nickname == "") binding.tvNicknameConfirm.text = ""
                setSignupBtnAble()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        binding.btnNicknameDuplicationCheck.setOnClickListener {
            if (verifyInput("nickname", nickname)) {
                verifiyingNickname = nickname
                signupViewModel.checkNicknameDuplicate(verifiyingNickname)
            } else {
                signupCheck["nickname"] = false
                binding.tvNicknameConfirm.text = "사용 불가능한 닉네임입니다."
                binding.tvNicknameConfirm.setTextColor(Color.RED)
            }
        }

        signupViewModel.checkNicknameResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onCheckNicknameSuccess(verifiyingNickname, it.data)
                is BaseResponse.Error -> onError(TAG, CHECK_NICKNAME, it.errorBody, it.msg)
                else -> onException(TAG, CHECK_NICKNAME, it.toString())
            }
        }
    }

    fun setUpUsername() {
        var username = ""
        var verifiyingUsername = ""
        binding.etUsername.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                username = binding.etUsername.text.toString()
                if (checkedUsername != "" && username == checkedUsername) {
                    binding.tvUsernameConfirm.text = "사용가능한 아이디입니다."
                    binding.tvUsernameConfirm.setTextColor(Color.BLACK)
                    signupCheck["username"] = true
                } else {
                    binding.tvUsernameConfirm.text = "아이디 중복확인이 필요합니다."
                    binding.tvUsernameConfirm.setTextColor(Color.RED)
                    signupCheck["username"] = false
                }
                if (username == "") {
                    binding.tvUsernameConfirm.text = ""
                    binding.tvUsernameConfirm.setTextColor(Color.BLACK)
                    signupCheck["username"] = false
                }
                setSignupBtnAble()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })

        binding.btnUsernameDuplicationCheck.setOnClickListener {
            if (verifyInput("username", username)) {
                verifiyingUsername = username
                signupViewModel.checkUsernameDuplicate(verifiyingUsername)
            } else {
                signupCheck["username"] = false
                binding.tvUsernameConfirm.text = "사용 불가능한 아이디입니다."
                binding.tvUsernameConfirm.setTextColor(Color.RED)
            }
        }

        signupViewModel.checkUsernameResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onCheckUsernameSuccess(verifiyingUsername, it.data)
                is BaseResponse.Error -> onError(TAG, CHECK_USERNAME, it.errorBody, it.msg)
                else -> onException(TAG, CHECK_USERNAME, it.toString())
            }
        }
    }

    fun setUpPassword() {
        binding.etPasswordConfirm.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) { compairPassword() }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
        binding.etPassword.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) { compairPassword() }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }

    fun setUpAccountNum() {
        binding.btnAccountNumDuplicationCheck.visibility = View.GONE
        binding.tvAccountNumConfirm.visibility = View.GONE
        val banks = resources.getStringArray(R.array.banks)
        bankName = banks[0]

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
        }}

    fun setUpEmail() {
        binding.tvCoolTime.visibility = View.GONE

        binding.etEmail.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                signupCheck["email"] = (verifiedEmail != "" && binding.etEmail.text.toString() == verifiedEmail)
                setSignupBtnAble()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        binding.btnSendCode.setOnClickListener {
            val email = binding.etEmail.text.toString()
            if (email != "" && isSendAble) {
                if (verifyInput("email", "${email}@pusan.ac.kr")) {
                    verifyingEmail = email
                    signupViewModel.sendVerificationCode(verifyingEmail)
                }
            }
            else if (verifyingEmail != "" && !isSendAble ) binding.tvCoolTime.visibility = View.VISIBLE

        }

        binding.btnVerifyEmail.setOnClickListener {
            if (verifyingEmail != "" && binding.etVerifyCode.text.toString() != "") {
                val email = "${verifyingEmail}@pusan.ac.kr"
                val verifyCode = binding.etVerifyCode.text.toString()
                signupViewModel.checkVerificationCode(email, verifyCode)
            }
        }

        signupViewModel.sendVerificationCodeResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onSendVerificationCodeSuccess()
                is BaseResponse.Error -> onError(TAG, SEND_CODE, it.errorBody, it.msg)
                else -> onException(TAG, SEND_CODE, it.toString())
            }
        }

        signupViewModel.checkVerificationCodeResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onCheckVerificationCodeSuccess(it.data)
                is BaseResponse.Error -> onError(TAG, CHECK_CODE, it.errorBody, it.msg)
                else -> onException(TAG, CHECK_CODE, it.toString())
            }
        }
    }

    fun setUpSignup() {
        binding.btnSignup.setOnClickListener {
            if (verifySignup()) {
                var signupForm = SignupForm(
                    authToken,
                    binding.etRealName.text.toString(),
                    binding.etUsername.text.toString(),
                    binding.etPassword.text.toString(),
                    binding.etNickname.text.toString(),
                    binding.etAccountNum.text.toString(),
                    "${binding.etEmail.text.toString()}@pusan.ac.kr"
                )
                signupViewModel.signup(signupForm)
            }
        }

        signupViewModel.signupResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onSignupSuccess()
                is BaseResponse.Error -> onError(TAG, SIGNUP, it.errorBody, it.msg)
                else -> onException(TAG, SIGNUP, it.toString())
            }
        }
    }

    fun onCheckNicknameSuccess(nickname: String, data: CheckDuplicateResponse?) {
        super.onSuccess()

        if (data == null) {
            onExceptionalProblem(TAG, CHECK_NICKNAME)
            return
        }

        if (data.isDuplicated) {
            signupCheck["nickname"] = false
            binding.tvNicknameConfirm.text = "사용 불가능한 닉네임입니다."
            binding.tvNicknameConfirm.setTextColor(Color.RED)
        } else {
            binding.tvNicknameConfirm.text = "사용 가능한 닉네임입니다."
            binding.tvNicknameConfirm.setTextColor(Color.BLACK)
            signupCheck["nickname"] = true
            checkedNickname = nickname
        }
        setSignupBtnAble()
    }

    fun onCheckUsernameSuccess(username: String, data: CheckDuplicateResponse?) {
        super.onSuccess()

        if (data == null) {
            onExceptionalProblem(TAG, CHECK_USERNAME)
            return
        }

        if (data.isDuplicated) {
            signupCheck["username"] = false
            binding.tvUsernameConfirm.text = "사용 불가능한 아이디입니다."
            binding.tvUsernameConfirm.setTextColor(Color.RED)
        } else {
            binding.tvUsernameConfirm.text = "사용 가능한 아이디입니다."
            binding.tvUsernameConfirm.setTextColor(Color.BLACK)
            signupCheck["username"] = true
            checkedUsername = username
        }
        setSignupBtnAble()
    }

    fun onSendVerificationCodeSuccess() {
        super.onSuccess()

        if (::job.isInitialized && job.isActive)
            job.cancel()
        job = GlobalScope.launch { countDown(valifyTime) }
        binding.btnVerifyEmail.setBackgroundResource(R.drawable.solid_primary)
        binding.tvVerifyEmail.setTextColor(Color.WHITE)
    }

    fun onCheckVerificationCodeSuccess(data: VerificationResponse?) {
        super.onSuccess()

        if (data == null) {
            onExceptionalProblem(TAG, CHECK_CODE)
            return
        }

        remainingSeconds = 0
        verifiedEmail = verifyingEmail
        signupCheck["email"] = true
        authToken = data.authToken
        setSignupBtnAble()
        Log.d("signUp Fragment - signupCheck", signupCheck.toString())
    }

    fun onSignupSuccess() {
        super.onSuccess()

        showToast("회원가입에 성공하였습니다.")
        navigateToLogin()
    }

    fun compairPassword() {
        if (binding.etPassword.text.toString() != "" && binding.etPasswordConfirm.text.toString() != "") {
            if (binding.etPassword.text.toString().equals(binding.etPasswordConfirm.text.toString())) {
                binding.tvPasswordConfirm.text = "비밀번호가 일치합니다."
                binding.tvPasswordConfirm.setTextColor(Color.BLACK)
                signupCheck["password"] = true
            } else {
                binding.tvPasswordConfirm.text = "비밀번호가 일치하지 않습니다."
                binding.tvPasswordConfirm.setTextColor(Color.RED)
                signupCheck["password"] = false
            }
        } else {
            binding.tvPasswordConfirm.text = ""
            signupCheck["password"] = false
        }
        setSignupBtnAble()
    }

    fun setSignupBtnAble() {
        if (signupCheck["username"]!! && signupCheck["password"]!! && signupCheck["nickname"]!! && signupCheck["email"]!!) {
            binding.btnSignup.setEnabled(true)
            binding.btnSignup.setBackgroundResource(R.drawable.solid_primary)
            binding.tvSignup.setTextColor(Color.WHITE)
        } else {
            binding.btnSignup.setEnabled(false)
            binding.btnSignup.setBackgroundResource(R.drawable.stroked_lightgray_silver)
            binding.tvSignup.setTextColor(Color.BLACK)
        }
    }

    suspend fun countDown(seconds: Int) {
        remainingSeconds = seconds
        isSendAble = false
        var remaingCoolTime = sendCoolTime

        while (remainingSeconds > 0) {
            withContext(Dispatchers.Main) {
                binding.tvVerifyCountdown.text = countDownStr(remainingSeconds)
                if (remaingCoolTime > 0) {
                    binding.tvCoolTime.text = "${remaingCoolTime}초 후에 재전송 가능합니다."
                    remaingCoolTime--
                } else {
                    binding.tvCoolTime.visibility = View.GONE
                    isSendAble = true
                }
            }
            delay(1000)

            remainingSeconds--
        }

        withContext(Dispatchers.Main) {
            when (remainingSeconds) {
                0 -> binding.tvVerifyCountdown.text = "만료되었습니다."
                -1 -> binding.tvVerifyCountdown.text = "인증되었습니다."
                else -> binding.tvVerifyCountdown.text = ""
            }
        }
    }

    fun countDownStr(seconds: Int): String {
        val min = seconds / 60
        val sec = seconds % 60
        return String.format("%02d:%02d", min, sec)
    }

    fun verifySignup(): Boolean {
        val builder = AlertDialog.Builder(fragmentContext)
        if (verifyInput("realName", binding.etRealName.text.toString())) {
            if (verifyInput("password", binding.etPassword.text.toString())) {
                if (verifyInput("accountNum", binding.etAccountNum.text.toString())) {
                    return true
                } else {
                    builder.setMessage("사용할 수 없는 계좌번호 형식입니다.")
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> })
                    builder.show()
                }
            } else {
                builder.setMessage("비밀번호는 숫자, 영문자, 특수문자(~!@#\$%^&)를 각각 하나이상 포함하여 8~16자여야 합니다.")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> })
                builder.show()
            }
        } else {
            builder.setMessage("사용할 수 없는 이름 형식입니다.")
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> })
            builder.show()
        }
        return false
    }

    fun navigateToLogin() {
        onBackPressed()
    }
}
