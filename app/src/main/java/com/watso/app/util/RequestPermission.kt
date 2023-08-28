package com.watso.app.util

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import com.watso.app.MainActivity

class RequestPermission(val activity: MainActivity) {
    private val TAG = "[권한 요청]"
    val PERMISSIONS_REQUEST_NOTIFICATION = 123

    interface NotiPermitChangedListener { fun onNotiPermitChanged() }
    private var notiPermitChangedListener: NotiPermitChangedListener? = null

    fun setNotiPermitChangedListener(listener: NotiPermitChangedListener) {
        this.notiPermitChangedListener = listener
    }

    fun requestNotificationPermission() {
        if (getStatus() == "") showPurposeDialog()
    }

    fun changeNotificationEnabled() {
        if (isNotificationEnabled()) {
            setStatus("false")
        } else {
            when (getStatus()) {
                "false" -> {
                    if (isNotificationPermitted())
                        setStatus("true")
                    else
                        setStatus("false")
                }
                else -> showPurposeDialog()
            }
        }
    }

    fun isNotificationPermitted(): Boolean {
        val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.areNotificationsEnabled()
    }

    fun isNotificationEnabled(): Boolean {
        Log.d("[$TAG] 알림가능여부","getPrefs: ${getStatus()}, isPermitted: ${isNotificationPermitted()}")
        if (getStatus() == "true") {
            if (isNotificationPermitted())
                return true
            else {
                setStatus("denied")
                return false
            }
        }
        return false
    }

    fun showPurposeDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("알림 권한 요청")
            .setMessage("게시글 관련 안내사항이나 댓글소식을 알림으로 전달받기 위해서 권한을 요청합니다.")
            .setPositiveButton("알림 설정", DialogInterface.OnClickListener { _, _ ->
                getNotiPermission()
                setStatus("true")
            })
            .setNegativeButton("거절", DialogInterface.OnClickListener { _, _ ->
                notiPermitChangedListener?.onNotiPermitChanged()
            })
            .setOnCancelListener { _ ->
                notiPermitChangedListener?.onNotiPermitChanged()
            }
        builder.show()
    }

    fun getNotiPermission() {
        if (getStatus() == "") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSIONS_REQUEST_NOTIFICATION
                )
            } else {
                makeIntent()
            }
        } else makeIntent()
    }

    fun makeIntent() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
        activity.startActivity(intent)
    }

    fun getStatus():String {
        return MainActivity.prefs.getString("useNotification", "")
    }

    fun setStatus(status: String) {
        MainActivity.prefs.setString("useNotification", status)
    }
}
