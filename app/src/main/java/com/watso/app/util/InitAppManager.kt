package com.watso.app.util

import android.util.Log
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.watso.app.ErrorResponse
import com.watso.app.FragmentHome
import com.watso.app.MainActivity
import com.watso.app.data.model.BaseResponse
import com.watso.app.feature.auth.ui.view.FragmentLogin
import com.watso.app.feature.baedal.ui.view.baedalList.FragmentBaedalList
import com.watso.app.feature.user.data.FcmToken
import com.watso.app.feature.user.data.UserInfo
import com.watso.app.feature.user.ui.view.FragmentAccount
import okhttp3.ResponseBody

private const val GET_USER_INFO = "유저정보 갱신"
private const val SEND_FCM_TOKEN = "디바이스 토큰 갱신"

class InitAppManager(
    private val initAppViewModel: InitAppViewModel,
    private val mActivity: MainActivity
) {

    private val AC = ActivityController(mActivity)
    private val TAG = "[InitApp]"
    private var tempFcmToken = ""
    private var isInitializing = true

    init {
        setObservers()
    }

    fun initApp() {
        val refreshToken = SessionManager.getRefreshToken(mActivity)
        val accessToken = SessionManager.getAccessToken(mActivity)


        if (refreshToken != "") {
            Log.d("$TAG access token", accessToken)
            Log.d("$TAG refresh token", refreshToken)
            getUserInfo()
        } else {
            navigateToLogin()
        }
    }

    private fun setObservers() {
        initAppViewModel.userInfo.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onGetUserInfoSuccess(it.data)
                is BaseResponse.Error -> onError(GET_USER_INFO, it.errorBody, it.msg, true)
                else -> onException(GET_USER_INFO, it.toString(), true)
            }
        }

        initAppViewModel.sendFcmTokenResult.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onSendFcmTokenSuccess()
                is BaseResponse.Error -> onError(SEND_FCM_TOKEN, it.errorBody, it.msg)
                else -> onException(SEND_FCM_TOKEN, it.toString())
            }
        }
    }

    private fun onLoading() {
        showProgressBar()
    }

    fun onError(method: String, errorBody: ResponseBody?, msg: String?, isAuthenticFailed: Boolean = false) {
        hideProgressBar()

        if (errorBody != null) {
            val gson = Gson()
            val errorBodyObject = gson.fromJson(errorBody.string(), ErrorResponse::class.java)
            Log.e(TAG,"$method ${ErrorString.FAIL} (${errorBodyObject.msg} - ${errorBodyObject.code})")
            showToast(errorBodyObject.msg)
        }

        else if (msg == null) {
            val errMsg = "$method ${ErrorString.FAIL}: ${ErrorString.E5001}"
            Log.e(TAG, errMsg)
            showToast(errMsg)
        }

        if (isAuthenticFailed) {
            AC.logOut(null)
        }
    }

    fun onException(method: String, exMsg: String, isAuthenticFailed: Boolean = false) {
        hideProgressBar()

        Log.e(TAG, "$method ${ErrorString.FAIL}, $exMsg")
        showToast("$method ${ErrorString.FAIL}")

        if (isAuthenticFailed) {
            AC.logOut(null)
        }
    }

    fun getUserInfo(isInitializing: Boolean = true) {
        this.isInitializing = isInitializing
        initAppViewModel.getUserInfo()
    }

    private fun onGetUserInfoSuccess(data: UserInfo?) {
        hideProgressBar()

        if (data == null) {
            val errMsg = "$GET_USER_INFO ${ErrorString.FAIL}: ${ErrorString.E5002}"
            Log.e(TAG, errMsg)
            showToast(errMsg)
            return
        }

        saveUserInfo(data)

        checkFcmToken()
    }

    private fun saveUserInfo(userInfo: UserInfo) {
        val METHOD = "[setUserInfo] "
        Log.d("$TAG $METHOD", userInfo.toString())
        SessionManager.saveUserInfo(mActivity, userInfo)
    }

    private fun checkFcmToken() {
        val METHOD = "[checkFCMToken]"
        Log.d("$TAG $METHOD", "")

        showProgressBar()
        val messagingService = MyFirebaseMessagingService()
        messagingService.getFirebaseToken { token ->
            if (token != null) {
                hideProgressBar()
                val previous = AC.getString("previousFcmToken", "")
                Log.d("$TAG $METHOD", "previous: $previous, current: $token")

                if (token != previous) {
                    sendFcmToken(token)
                } else completeGetUserInfo()
            } else {
                hideProgressBar()
                Log.e("$TAG $METHOD", "Error retrieving Firebase Token")
                completeGetUserInfo()
            }
        }
    }

    private fun sendFcmToken(token: String) {
        val METHOD = "sendFcmToken"
        Log.d("[$TAG][$METHOD]", "token: $token")
        tempFcmToken = token
        initAppViewModel.sendFcmToken(FcmToken(token))
    }

    private fun onSendFcmTokenSuccess() {
        val METHOD = "onSendFcmTokenSuccess"
        hideProgressBar()
        val token = tempFcmToken
        Log.d("[$TAG][$METHOD]", "token: $token")
        AC.setString("previousFcmToken", token)
        completeGetUserInfo()
    }

    private fun navigateTo(fragment: Fragment, popBackStack: Int = 0) {
        AC.navigateTo(fragment, popBackStack = popBackStack)
    }

    private fun navigateToHome() {
        navigateTo(FragmentHome())
    }

    private fun completeGetUserInfo() {
        if (isInitializing) {
            navigateTo(FragmentBaedalList())
        } else {
            navigateTo(FragmentAccount(), 2)
        }
    }

    private fun navigateToLogin() {
        navigateTo(FragmentLogin())
    }

    private fun showProgressBar() {
        AC.showProgressBar()
    }

    private fun hideProgressBar() {
        AC.hideProgressBar()
    }

    fun showToast(msg: String) {
        AC.showToast(msg)
    }
}
