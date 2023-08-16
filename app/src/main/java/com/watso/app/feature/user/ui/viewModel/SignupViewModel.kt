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

    private fun <T> makeRequest(
        liveData: MutableLiveData<BaseResponse<T>>,
        request: suspend () -> retrofit2.Response<T>
    ) {
        liveData.value = BaseResponse.Loading()
        viewModelScope.launch {
            try {
                val response = request()
                if (response.isSuccessful) {
                    liveData.value = BaseResponse.Success(response.body())
                } else {
                    liveData.value = BaseResponse.Error(response.message())
                }
            } catch (ex: Exception) {
                liveData.value = BaseResponse.Error(ex.message)
            }
        }
    }

    fun checkNicknameDuplicate(nickname: String) {
        makeRequest(checkNicknameResponse) { userRepo.checkDuplicate("nickname", nickname) }
    }

    fun checkUsernameDuplicate(username: String) {
        makeRequest(checkUsernameResponse) { userRepo.checkDuplicate("username", username) }
    }

    fun sendVerificationCode(email: String) {
        makeRequest(sendVerificationCodeResponse) { userRepo.sendVerificationCode(email) }
    }

    fun checkVerificationCode(email: String, verifyCode: String) {
        makeRequest(checkVerificationCodeResponse) { userRepo.checkVerificationCode(email, verifyCode) }
    }

    fun signup(signupForm: SignupForm) {
        makeRequest(signupResponse) { userRepo.signup(signupForm) }
    }
}
