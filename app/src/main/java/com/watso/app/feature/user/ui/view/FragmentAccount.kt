package com.watso.app.feature.user.ui.view

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.watso.app.BaseFragment
import com.watso.app.data.model.BaseResponse
import com.watso.app.util.RequestPermission
import com.watso.app.databinding.FragAccountBinding
import com.watso.app.feature.user.data.NotificationSubscription
import com.watso.app.feature.user.data.UserInfo
import com.watso.app.feature.user.ui.viewModel.AccountViewModel
import com.watso.app.util.SessionManager

private const val GET_NOTIFICATION_STATUS_FAIL = "알림 수신 여부 조회 실패"
private const val UPDATE_NOTIFICATION_STATUS_FAIL = "알림 수신 여부 변경 실패"
private const val DELETE_ACCOUNT_FAIL = "계정 삭제 실패"
private const val LOGOUT_FAIL = "로그아웃 실패"

class FragmentAccount :BaseFragment() {

    lateinit var RP: RequestPermission
    lateinit var fragmentContext: Context
    lateinit var userInfo: UserInfo

    var mBinding: FragAccountBinding? = null
    val binding get() = mBinding!!
    val TAG = "FragAccount"
    val accountViewModel by viewModels<AccountViewModel>()

    var notificationSwitchBefore = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onResume() {
        super.onResume()
        val requestPermitted = RP.isNotificationEnabled()
        Log.d("[$TAG]onResusme", requestPermitted.toString())

        bindSWNotificationPermission()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragAccountBinding.inflate(inflater, container, false)

        RP = RequestPermission(mActivity)
        RP.setNotiPermitChangedListener(object: RequestPermission.NotiPermitChangedListener {
            override fun onNotiPermitChanged() { bindSWNotificationPermission() }
        })

        setUpUI()
        setClickListeners()
        setObservers()

        return binding.root
    }

    fun setUpUI() {
        val userInfo = SessionManager.getUserInfo(mActivity)
        binding.tvRealName.text = userInfo.name
        binding.tvUsername.text = userInfo.username
        binding.tvEmail.text = userInfo.email
        binding.tvNickname.text = userInfo.nickname
        binding.tvAccountNum.text = userInfo.accountNumber
    }

    fun setClickListeners() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }

//        binding.lytPassword.setOnClickListener { navigateTo(FragmentUpdateAccount(), mapOf("target" to "password")) }
//        binding.lytNickname.setOnClickListener { navigateTo(FragmentUpdateAccount(), mapOf("target" to "nickname")) }
//        binding.lytAccountNum.setOnClickListener { navigateTo(FragmentUpdateAccount(), mapOf("target" to "accountNum")) }

        binding.swNotification.setOnCheckedChangeListener { _, isChecked -> changeNotificationEnabled(isChecked)}

        binding.lytOpenTalk.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://open.kakao.com/o/gE07iSmf")))
        }
        binding.lytOss.setOnClickListener {
            startActivity(Intent(fragmentContext, OssLicensesMenuActivity::class.java))
            OssLicensesMenuActivity.setActivityTitle("오픈소스 라이선스")
        }
        binding.lytLogout.setOnClickListener {
            val builder = AlertDialog.Builder(fragmentContext)
            builder.setTitle("로그아웃하기")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("확인", DialogInterface.OnClickListener { _, _ -> logout() })
                .setNegativeButton("취소", DialogInterface.OnClickListener { _, _  -> })
            builder.show()
        }
        binding.lytDeleteAccount.setOnClickListener {
            val builder = AlertDialog.Builder(fragmentContext)
            builder.setTitle("회원 탈퇴하기")
                .setMessage("탈퇴 하시겠습니까?")
                .setPositiveButton("확인", DialogInterface.OnClickListener { _, _  ->
                    deleteAccount() })
                .setNegativeButton("취소", DialogInterface.OnClickListener { _, _  -> })
            builder.show()
        }
    }

    fun setObservers() {
        accountViewModel.getNotificationStatusResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onGetNotificationStatusSuccess(it.data)
                is BaseResponse.Error -> onError(GET_NOTIFICATION_STATUS_FAIL, it.msg, TAG)
                else -> onException(GET_NOTIFICATION_STATUS_FAIL, it.toString(), TAG)
            }
        }

        accountViewModel.updateNotificationStatusResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onUpdateNotificationStatusSuccess()
                is BaseResponse.Error -> onError(UPDATE_NOTIFICATION_STATUS_FAIL, it.msg, TAG)
                else -> onException(UPDATE_NOTIFICATION_STATUS_FAIL, it.toString(), TAG)
            }
        }

        accountViewModel.deleteAccountResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onDeleteAccountSuccess()
                is BaseResponse.Error -> onError(DELETE_ACCOUNT_FAIL, it.msg, TAG)
                else -> onException(DELETE_ACCOUNT_FAIL, it.toString(), TAG)
            }
        }

        accountViewModel.logoutResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onLogoutSuccess()
                is BaseResponse.Error -> onError(LOGOUT_FAIL, it.msg, TAG)
                else -> onException(LOGOUT_FAIL, it.toString(), TAG)
            }
        }
    }

    fun onGetNotificationStatusSuccess(subscription: NotificationSubscription?) {
        super.onSuccess()

        if (subscription == null) {
            onExceptionalProblem(TAG)
            return
        }

        if (subscription.status && RP.isNotificationEnabled()) {
            notificationSwitchBefore = true
            binding.swNotification.isChecked = true
        } else {
            notificationSwitchBefore = false
            binding.swNotification.isChecked = false
        }
    }

    fun onUpdateNotificationStatusSuccess() {
        super.onSuccess()
    }

    fun onDeleteAccountSuccess() {
        super.onSuccess()
        AC.logOut("정상적으로 탈퇴되었습니다.")
    }

    fun onLogoutSuccess() {
        super.onSuccess()
        AC.logOut("로그아웃 되었습니다.")
    }

    fun bindSWNotificationPermission() {
        if (RP.isNotificationEnabled()) {
            notificationSwitchBefore = true
            binding.swNotification.isChecked = true
        } else {
            notificationSwitchBefore = false
            binding.swNotification.isChecked = false
        }

        accountViewModel.getNotificationStatus()
    }

    fun changeNotificationEnabled(isChecked: Boolean) {
        Log.d(TAG, isChecked.toString())
        if (notificationSwitchBefore != isChecked) {
            val notificationSubscription = NotificationSubscription(isChecked)
            accountViewModel.updateNotificationStatus(notificationSubscription)

            RP.changeNotificationEnabled()
            notificationSwitchBefore = isChecked
        }
    }

    fun logout() {
        accountViewModel.logout()
    }

    fun deleteAccount() {
        accountViewModel.deleteAccount()
    }
}
