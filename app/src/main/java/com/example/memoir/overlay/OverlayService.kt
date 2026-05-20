package com.example.memoir.overlay

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.memoir.MainActivity
import com.example.memoir.R
import com.example.memoir.data.ThemeRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OverlayService : Service() {

    @Inject
    lateinit var themeRepository: ThemeRepository

    private var windowManager: WindowManager? = null
    private var overlayView: OverlayHandleView? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var isAppInForeground = false
    private var isOverlayEnabledPref = false

    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        private var startedActivities = 0

        override fun onActivityStarted(activity: Activity) {
            startedActivities++
            isAppInForeground = startedActivities > 0
            updateOverlayVisibility()
        }

        override fun onActivityStopped(activity: Activity) {
            startedActivities = (startedActivities - 1).coerceAtLeast(0)
            isAppInForeground = startedActivities > 0
            updateOverlayVisibility()
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }

    companion object {
        private const val NOTIFICATION_ID = 9912
        private const val CHANNEL_ID = "memoir_quick_action"
        const val ACTION_HIDE_OVERLAY = "com.example.memoir.ACTION_HIDE_OVERLAY"
        const val ACTION_SHOW_OVERLAY = "com.example.memoir.ACTION_SHOW_OVERLAY"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        isAppInForeground = checkIfAppIsInForeground()
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        createNotificationChannel()
        startServiceForeground()
        showOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                ACTION_HIDE_OVERLAY -> {
                    isAppInForeground = true
                    updateOverlayVisibility()
                }
                ACTION_SHOW_OVERLAY -> {
                    isAppInForeground = false
                    updateOverlayVisibility()
                }
            }
        }
        return START_STICKY
    }

    private fun checkIfAppIsInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }

    private fun startServiceForeground() {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Memoir Quick Action",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Runs the quick action overlay for creating chronicle entries"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Memoir Quick Action")
            .setContentText("Swipe edge handle to quick-create entry")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun showOverlay() {
        val density = resources.displayMetrics.density
        val windowWidth = (48 * density).toInt()
        val windowHeight = (100 * density).toInt()

        val params = WindowManager.LayoutParams(
            windowWidth,
            windowHeight,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 0
            y = (150 * density).toInt() // Position in upper-right
        }

        val handleView = OverlayHandleView(this)
        overlayView = handleView

        // Listen for theme updates to dynamically adjust colors
        serviceScope.launch {
            themeRepository.isDarkMode.collectLatest { isDark ->
                handleView.isDarkMode = isDark
            }
        }

        // Listen for preference flow updates to control visibility
        serviceScope.launch {
            themeRepository.isOverlayEnabled.collectLatest { enabled ->
                isOverlayEnabledPref = enabled
                updateOverlayVisibility()
            }
        }

        handleView.onSwipeTriggered = {
            val activityIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("EXTRA_OPEN_DESK", true)
            }
            
            // Use PendingIntent for privileged launches on modern Android versions
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, flags)
            
            @Suppress("DEPRECATION")
            val options = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                android.app.ActivityOptions.makeBasic().apply {
                    pendingIntentBackgroundActivityStartMode = 
                        android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                }.toBundle()
            } else {
                null
            }

            try {
                pendingIntent.send(this, 0, null, null, null, null, options)
            } catch (e: Exception) {
                try {
                    startActivity(activityIntent)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

        windowManager?.addView(handleView, params)
        updateOverlayVisibility()
    }

    private fun updateOverlayVisibility() {
        val shouldShow = isOverlayEnabledPref && !isAppInForeground
        overlayView?.visibility = if (shouldShow) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
        serviceScope.cancel()
        overlayView?.let {
            windowManager?.removeView(it)
        }
    }
}
