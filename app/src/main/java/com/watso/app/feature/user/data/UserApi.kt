package com.watso.app.feature.user.data

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface UserApi {

    companion object
    {
        const val SIGNUP = "user/signup"
        const val PROFILE = "user/profile"
        const val FORGOT = "user/forgot"
        const val DEVICE = "user/device"
    }


    /** 회원 가입 */

    @POST(SIGNUP)                    // 회원가입
    suspend fun signup(
        @Body jsonparams: SignupForm
    ): Response<ResponseBody>

    @GET(SIGNUP)                     // 메일 인증코드 발송 요청
    suspend fun sendVerificationCode(
        @Query("email") email: String
    ): Response<ResponseBody>

    @GET("$SIGNUP/validation-check")        // 인증코드 유효성 검사
    suspend fun checkVerificationCode(
        @Query("email") email: String,
        @Query("auth-code") verifyCode: String
    ): Response<VerificationResponse>


    /** 중복 체크 */

    @GET("user/duplicate-check")        // 정보 중복조회
    suspend fun checkDuplicate(
        @Query("field") field: String,
        @Query("value") value: String
    ): Response<CheckDuplicateResponse>


    /** 프로필 */

    @GET(PROFILE)                       // 유저 정보 조회
    suspend fun getUserInfo(): Response<UserInfo>

    @DELETE(PROFILE)                    // 회원 탈퇴
    suspend fun deleteAccount(): Response<ResponseBody>

    @PATCH("$PROFILE/account-number")   // 계좌번호 변경
    suspend fun updateAccountNumber(
        @Body jsonparams: AccountNumber
    ): Response<ResponseBody>

    @PATCH("$PROFILE/nickname")         // 닉네임 변경
    suspend fun updateNickname(
        @Body jsonparams: Nickname
    ): Response<ResponseBody>

    @PATCH("$PROFILE/password")         // 비밀번호 변경
    suspend fun updatePassword(
        @Body jsonparams: UpdatePasswordForm
    ): Response<ResponseBody>


    /** 찾기 */

    @POST("$FORGOT/password")           // 비밀번호 찾기
    suspend fun issueTempPassword(
        @Body jsonparams: ForgotPasswordForm
    ): Response<ResponseBody>

    @GET("$FORGOT/username")            // 계정 찾기
    suspend fun findUsername(
        @Query("email") email: String
    ): Response<ResponseBody>


    /** 기기 관리 */
    @PATCH("$DEVICE/token")                    // 기기 정보 추가
    suspend fun sendFcmToken(
        @Body jsonparams: FcmToken
    ): Response<ResponseBody>

    @GET("$DEVICE/notification")        // 알림 허용상태 조회
    suspend fun getNotificationStatus(
    ): Response<NotificationSubscription>

    @PATCH("$DEVICE/notification")      // 알림 허용상태 변경
    suspend fun updateNotificationStatus(
        @Body jsonparams: NotificationSubscription
    ): Response<ResponseBody>
}
