package com.watso.app.feature.auth.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.watso.app.data.model.BaseResponse
import com.watso.app.feature.auth.data.AuthRepository
import com.watso.app.feature.auth.data.LoginForm
import kotlinx.coroutines.launch
import okhttp3.Headers
import java.lang.Exception

class LoginViewModel(application: Application): AndroidViewModel(application) {

    private val authRepo = AuthRepository(application)
    val loginResponseHeaders: MutableLiveData<BaseResponse<Headers>> = MutableLiveData()

    fun login(username: String, password: String) {
        loginResponseHeaders.value = BaseResponse.Loading()
        viewModelScope.launch {
            try {
                val loginForm = LoginForm (username = username, password = password)
                val response = authRepo.login(loginForm)
                if (response.code() == 200) {
                    loginResponseHeaders.value = BaseResponse.Success(response.headers())
                } else {
                    loginResponseHeaders.value = BaseResponse.Error(response.message())
                }
            } catch (ex: Exception) {
                loginResponseHeaders.value = BaseResponse.Error(ex.message)
            }
        }
    }
}
