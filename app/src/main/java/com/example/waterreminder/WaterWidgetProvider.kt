package com.example.waterreminder

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.waterreminder.data.WaterDataManager

class WaterWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_ADD_WATER = "com.example.waterreminder.ACTION_ADD_WATER"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val dataManager = WaterDataManager(context)
        val count = dataManager.getTodayCount()
        val goal = dataManager.getDailyGoal().targetCups
        val progress = (count * 100 / goal).coerceAtMost(100)

        val views = RemoteViews(context.packageName, R.layout.widget_water)
        
        // 设置喝水数量
        views.setTextViewText(R.id.widget_count, "$count")
        views.setTextViewText(R.id.widget_goal, "/ $goal")
        
        // 设置进度条
        views.setProgressBar(R.id.widget_progress, 100, progress, false)
        
        // 设置点击添加水
        val addIntent = Intent(context, WaterWidgetProvider::class.java).apply {
            action = ACTION_ADD_WATER
        }
        val addPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            addIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_add_water, addPendingIntent)
        
        // 点击打开应用
        val openIntent = Intent(context, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_layout, openPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_ADD_WATER) {
            val dataManager = WaterDataManager(context)
            dataManager.addWaterRecord()
            
            // 更新所有 widget
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = android.content.ComponentName(context, WaterWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
}
