package com.watso.app.feature.user.data

import com.google.gson.annotations.SerializedName


/** 회원 가입 */

data class SignupForm(
    @SerializedName("auth_token")
    val authToken: String,
    val name: String,
    val username: String,
    @SerializedName("pw")
    val password: String,
    val nickname: String,
    @SerializedName("account_number")
    val accountNumber: String,
    val email: String
)

data class VerificationResponse(
    @SerializedName("auth_token")
    val authToken: String
)


/** 중복 체크 */

data class CheckDuplicateResponse(
    @SerializedName("is_duplicated")
    val isDuplicated: Boolean
)


/** 프로필 */

data class UserInfo(
    val _id: Long,
    val name: String,
    val username: String,
    val nickname: String,
    @SerializedName("account_number")
    val accountNumber: String,
    val email: String
)

data class AccountNumber(
    @SerializedName("account_number")
    val accountNumber: String
)

data class Nickname(
    val nickname: String
)

data class UpdatePasswordForm(
    @SerializedName("current_password")
    val currentPassword: String,
    @SerializedName("new_password")
    val newPassword: String
)


/** 찾기 */

data class ForgotPasswordForm(
    val username: String,
    val email: String
)


/** 기기 관리 */

data class FcmToken(
    @SerializedName("device_token")
    val fcmToken: String
)

data class NotificationSubscribe(
    @SerializedName("allow")
    val status: Boolean
)
