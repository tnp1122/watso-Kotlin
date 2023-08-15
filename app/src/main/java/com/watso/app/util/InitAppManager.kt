package com.watso.app.util

import android.util.Log
import androidx.fragment.app.Fragment
import com.watso.app.FragmentHome
import com.watso.app.MainActivity
import com.watso.app.data.model.BaseResponse
import com.watso.app.feature.auth.ui.view.FragmentLogin
import com.watso.app.feature.user.data.FcmToken
import com.watso.app.feature.user.data.UserInfo

private const val GET_USER_INFO_FAIL = "유저정보 갱신 실패"
private const val SEND_FCM_TOKEN_FAIL = "디바이스 토큰 갱신 실패"

class InitAppManager(
    private val initAppViewModel: InitAppViewModel,
    private val mActivity: MainActivity
) {

    private val AC = ActivityController(mActivity)
    private val TAG = "[InitApp]"
    private var tempFcmToken = ""

    fun initApp() {
        val refreshToken = SessionManager.getRefreshToken(mActivity)
        val accessToken = SessionManager.getAccessToken(mActivity)

        setObservers()

        if (refreshToken != "") {
            Log.d("$TAG access token", accessToken)
            Log.d("$TAG refresh token", refreshToken)
            initAppViewModel.getUserInfo()
        } else {
            navigateToLogin()
        }
    }

    private fun setObservers() {
        initAppViewModel.userInfo.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onGetUserInfoSuccess(it.data)
                is BaseResponse.Error -> onError(GET_USER_INFO_FAIL, it.msg, true)
                else -> onException(GET_USER_INFO_FAIL, it.toString(), true)
            }
        }

        initAppViewModel.sendFcmTokenResult.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onSendFcmTokenSuccess()
                is BaseResponse.Error -> onError(SEND_FCM_TOKEN_FAIL, it.msg)
                else -> onException(SEND_FCM_TOKEN_FAIL, it.toString())
            }
        }
    }

    private fun onLoading() {
        showProgressBar()
    }

    fun onError(msg: String, errMsg: String?, isAuthenticFailed: Boolean = false) {
        hideProgressBar()

        if (isAuthenticFailed) {
            AC.logOut("$msg: $errMsg")
            return
        }

        if (errMsg == null) {
            showToast("$msg: ${ErrorString.E5001}")
            return
        }

        Log.e(TAG, "onError: $msg")
        showToast(msg)
    }

    fun onException(msg: String, exMsg: String?, isAuthenticFailed: Boolean = false) {
        hideProgressBar()

        if (isAuthenticFailed) {
            AC.logOut("$msg: $exMsg")
            return
        }

        if (exMsg == null) {
            showToast("$msg: ${ErrorString.E5000}")
            return
        }

        Log.e(TAG, "$msg: $exMsg")
        showToast("$msg: $exMsg")
    }

    private fun onGetUserInfoSuccess(data: UserInfo?) {
        hideProgressBar()

        val METHOD = "[onGetUserInfoSuccess]"
        Log.d("$TAG $METHOD", "")

        if (data == null) {
            showToast("$GET_USER_INFO_FAIL: ${ErrorString.E5002}")
            return
        }

        Log.d(TAG + "setUserInfo", data.toString())
        setUserInfo(data)

        checkFcmToken()
    }

    private fun setUserInfo(userInfo: UserInfo) {
        val METHOD = "[setUserInfo]"
        Log.d("$TAG $METHOD", userInfo.toString())
        AC.setString("name", userInfo.name)
        AC.setString("nickname", userInfo.nickname)
        AC.setString("accountNum", userInfo.accountNumber)
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
                } else navigateToHome()
            } else {
                hideProgressBar()
                Log.e("$TAG $METHOD", "Error retrieving Firebase Token")
                navigateToHome()
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
        navigateToHome()
    }

    private fun navigateTo(fragment: Fragment, fragindex: Int) {
        AC.navigateTo(fragment, fragIndex = fragindex)
    }

    private fun navigateToHome() {
        navigateTo(FragmentHome(), 1)
    }

    private fun navigateToLogin() {
        navigateTo(FragmentLogin(), 0)
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
