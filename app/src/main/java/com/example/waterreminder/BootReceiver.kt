package com.example.waterreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.AlarmManager
import android.app.PendingIntent
import com.example.waterreminder.data.WaterDataManager

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            // 开机后重新设置提醒
            scheduleReminderAfterBoot(context)
        }
    }

    private fun scheduleReminderAfterBoot(context: Context) {
        // 使用 WaterDataManager 获取设置
        val dataManager = WaterDataManager(context)
        val goal = dataManager.getDailyGoal()
        
        // 获取提醒间隔（从旧 SharedPreferences 读取兼容）
        val prefs = context.getSharedPreferences("water_prefs", Context.MODE_PRIVATE)
        val intervalMinutes = prefs.getInt("interval_minutes", 60)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WaterReminderReceiver::class.java).apply {
            action = "com.example.waterreminder.REMINDER"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + intervalMinutes * 60 * 1000

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
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
}
