package com.watso.app.feature.user.data

import android.content.Context
import com.watso.app.data.network.ApiClient
import okhttp3.ResponseBody
import retrofit2.Response

class UserRepository(context: Context) {

    private val api = ApiClient.create(context)


    /** 회원 가입 */

    suspend fun signup(signupForm: SignupForm): Response<ResponseBody> {
        return api.signup(signupForm)
    }

    suspend fun sendVerificationCode(email: String): Response<ResponseBody> {
        return api.sendVerificationCode(email)
    }

    suspend fun checkVerificationCode(email: String, verifyCode: String): Response<VerificationResponse> {
        return api.checkVerificationCode(email, verifyCode)
    }


    /** 중복 체크 */

    suspend fun checkDuplicate(field: String, value: String): Response<CheckDuplicateResponse> {
        return api.checkDuplicate(field, value)
    }


    /** 프로필 */

    suspend fun getUserInfo(): Response<UserInfo> {
        return api.getUserInfo()
    }

    suspend fun deleteAccount(): Response<ResponseBody> {
        return api.deleteAccount()
    }

    suspend fun updateAccountNumber(accountNumber: AccountNumber): Response<ResponseBody> {
        return api.updateAccountNumber(accountNumber)
    }

    suspend fun updateNickname(nickname: Nickname): Response<ResponseBody> {
        return api.updateNickname(nickname)
    }

    suspend fun updatePassword(updatePasswordForm: UpdatePasswordForm): Response<ResponseBody> {
        return api.updatePassword(updatePasswordForm)
    }


    /** 찾기 */

    suspend fun issueTempPassword(forgotPasswordForm: ForgotPasswordForm): Response<ResponseBody> {
        return api.issueTempPassword(forgotPasswordForm)
    }

    suspend fun findUsername(email: String): Response<ResponseBody> {
        return api.findUsername(email)
    }


    /** 기기 관리 */

    suspend fun sendFcmToken(fcmToken: FcmToken): Response<ResponseBody> {
        return api.sendFcmToken(fcmToken)
    }

    suspend fun getNotificationStatus(): Response<NotificationSubscription> {
        return api.getNotificationStatus()
    }

    suspend fun updateNotificationStatus(notificationSubscribe: NotificationSubscription): Response<ResponseBody> {
        return api.updateNotificationStatus(notificationSubscribe)
    }
}
