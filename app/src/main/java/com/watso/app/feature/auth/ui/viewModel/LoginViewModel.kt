package com.watso.app.feature.auth.ui.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.watso.app.data.model.BaseResponse
import com.watso.app.feature.auth.data.AuthRepository
import com.watso.app.feature.auth.data.LoginForm
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.lang.Exception

class LoginViewModel(application: Application): AndroidViewModel(application) {

    private val authRepo = AuthRepository(application)
    val loginResponse: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()

    fun login(username: String, password: String) {
        loginResponse.value = BaseResponse.Loading()
        viewModelScope.launch {
            try {
                val loginForm = LoginForm (username = username, password = password)
                val response = authRepo.login(loginForm)
                if (response.code() == 200) {
                    loginResponse.value = BaseResponse.Success(response.body())
                } else {
                    loginResponse.value = BaseResponse.Error(response.message())
                }
            } catch (ex: Exception) {
                loginResponse.value = BaseResponse.Error(ex.message)
            }
        }
    }
}
