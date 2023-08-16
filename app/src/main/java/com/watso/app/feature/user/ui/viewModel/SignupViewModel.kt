package com.watso.app.feature.user.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.watso.app.data.model.BaseResponse
import com.watso.app.feature.user.data.CheckDuplicateResponse
import com.watso.app.feature.user.data.SignupForm
import com.watso.app.feature.user.data.UserRepository
import com.watso.app.feature.user.data.VerificationResponse
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.lang.Exception

class SignupViewModel(application: Application): AndroidViewModel(application) {

    private val userRepo = UserRepository(application)
    val checkNicknameResponse: MutableLiveData<BaseResponse<CheckDuplicateResponse>> = MutableLiveData()
    val checkUsernameResponse: MutableLiveData<BaseResponse<CheckDuplicateResponse>> = MutableLiveData()
    val sendVerificationCodeResponse: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()
    val checkVerificationCodeResponse: MutableLiveData<BaseResponse<VerificationResponse>> = MutableLiveData()
    val signupResponse: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()

    fun checkNicknameDuplicate(nickname: String) {
        checkNicknameResponse.value = BaseResponse.Loading()
        viewModelScope.launch {
            try {
                val response = userRepo.checkDuplicate("nickname", nickname)
                if (response.code() == 200) {
                    checkNicknameResponse.value = BaseResponse.Success(response.body())
                } else {
                    checkNicknameResponse.value = BaseResponse.Error(response.message())
                }
            } catch (ex: Exception) {
                checkNicknameResponse.value = BaseResponse.Error(ex.message)
            }
        }
    }

    fun checkUsernameDuplicate(username: String) {
        checkUsernameResponse.value = BaseResponse.Loading()
        viewModelScope.launch {
            try {
                val response = userRepo.checkDuplicate("username", username)
                if (response.code() == 200) {
                    checkUsernameResponse.value = BaseResponse.Success(response.body())
                } else {
                    checkUsernameResponse.value = BaseResponse.Error(response.message())
                }
            } catch (ex: Exception) {
                checkUsernameResponse.value = BaseResponse.Error(ex.message)
            }
        }
    }

    fun sendVerificationCode(email: String) {
        sendVerificationCodeResponse.value = BaseResponse.Loading()
        viewModelScope.launch {
            try {
                val response = userRepo.sendVerificationCode(email)
                if (response.code() == 204) {
                    sendVerificationCodeResponse.value = BaseResponse.Success(response.body())
                } else {
                    sendVerificationCodeResponse.value = BaseResponse.Error(response.message())
                }
            } catch (ex: Exception) {
                sendVerificationCodeResponse.value = BaseResponse.Error(ex.message)
            }
        }
    }

    fun checkVerificationCode(email: String, verifyCode: String) {
        checkVerificationCodeResponse.value = BaseResponse.Loading()
        viewModelScope.launch {
            try {
                val response = userRepo.checkVerificationCode(email, verifyCode)
                if (response.code() == 200) {
                    checkVerificationCodeResponse.value = BaseResponse.Success(response.body())
                } else {
                    checkVerificationCodeResponse.value = BaseResponse.Error(response.message())
                }
            } catch (ex: Exception) {
                checkVerificationCodeResponse.value = BaseResponse.Error(ex.message)
            }
        }
    }

    fun signup(signupForm: SignupForm) {
        signupResponse.value = BaseResponse.Loading()
        viewModelScope.launch {
            try {
                val response = userRepo.signup(signupForm)
                if (response.code() == 201) {
                    signupResponse.value = BaseResponse.Success(response.body())
                } else {
                    signupResponse.value = BaseResponse.Error(response.message())
                }
            } catch (ex: Exception) {
                signupResponse.value = BaseResponse.Error(ex.message)
            }
        }
    }
}
