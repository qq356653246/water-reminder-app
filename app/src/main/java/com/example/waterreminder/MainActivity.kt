package com.example.waterreminder

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var countText: TextView
    private lateinit var nextTimeText: TextView
    private lateinit var drinkButton: Button
    private lateinit var settingsButton: Button
    
    private lateinit var prefs: SharedPreferences
    private var reminderIntervalMinutes = 60 // 默认 1 小时

    companion object {
        const val CHANNEL_ID = "water_reminder_channel"
        const val REQUEST_CODE = 1001
        const val PREFS_NAME = "water_prefs"
        const val KEY_COUNT = "today_count"
        const val KEY_LAST_DATE = "last_date"
        const val KEY_INTERVAL = "interval_minutes"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        reminderIntervalMinutes = prefs.getInt(KEY_INTERVAL, 60)

        // 初始化视图
        countText = findViewById(R.id.countText)
        nextTimeText = findViewById(R.id.nextTimeText)
        drinkButton = findViewById(R.id.drinkButton)
        settingsButton = findViewById(R.id.settingsButton)

        // 创建通知渠道
        createNotificationChannel()

        // 请求通知权限（Android 13+）
        requestNotificationPermission()

        // 检查是否是新的一天，重置计数
        checkNewDay()

        // 更新界面
        updateUI()

        // 设置提醒
        scheduleReminder()

        // 按钮点击事件
        drinkButton.setOnClickListener {
            drinkWater()
        }

        settingsButton.setOnClickListener {
            showSettings()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_description)
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE
                )
            }
        }
    }

    private fun checkNewDay() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastDate = prefs.getString(KEY_LAST_DATE, "")
        if (lastDate != today) {
            prefs.edit().apply {
                putInt(KEY_COUNT, 0)
                putString(KEY_LAST_DATE, today)
                apply()
            }
        }
    }

    private fun updateUI() {
        val count = prefs.getInt(KEY_COUNT, 0)
        countText.text = count.toString()

        // 计算下次提醒时间
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, reminderIntervalMinutes)
        }
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        nextTimeText.text = timeFormat.format(calendar.time)
    }

    private fun drinkWater() {
        val count = prefs.getInt(KEY_COUNT, 0) + 1
        prefs.edit().putInt(KEY_COUNT, count).apply()
        
        countText.text = count.toString()
        Toast.makeText(this, "已记录一杯水！💧", Toast.LENGTH_SHORT).show()
        
        // 重新设置提醒（从喝水时间重新计时）
        scheduleReminder()
        updateUI()
    }

    private fun scheduleReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, WaterReminderReceiver::class.java).apply {
            action = "com.example.waterreminder.REMINDER"
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + reminderIntervalMinutes * 60 * 1000

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    private fun showSettings() {
        // 简单设置：修改提醒间隔
        val intervals = arrayOf("30 分钟", "60 分钟", "90 分钟", "120 分钟")
        val intervalValues = arrayOf(30, 60, 90, 120)
        
        val currentIndex = intervalValues.indexOf(reminderIntervalMinutes)
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.reminder_interval)
            .setSingleChoiceItems(intervals, currentIndex) { dialog, which ->
                reminderIntervalMinutes = intervalValues[which]
                prefs.edit().putInt(KEY_INTERVAL, reminderIntervalMinutes).apply()
                scheduleReminder()
                updateUI()
                dialog.dismiss()
            }
            .setPositiveButton(R.string.save) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        checkNewDay()
        updateUI()
    }

    // 发送通知
    fun sendNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this).notify(1, notification)
    }
}
