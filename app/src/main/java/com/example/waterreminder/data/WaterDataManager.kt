package com.example.waterreminder.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * 喝水数据管理器
 */
class WaterDataManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        const val PREFS_NAME = "water_data"
        const val KEY_HISTORY = "water_history"
        const val KEY_DAILY_GOAL = "daily_goal"
        const val KEY_TODAY_COUNT = "today_count"
        const val KEY_LAST_DATE = "last_date"
        const val KEY_CUP_SIZE = "cup_size"
        const val DEFAULT_CUP_SIZE = 250
    }
    
    /**
     * 获取今日喝水次数
     */
    fun getTodayCount(): Int {
        val today = getTodayString()
        val lastDate = prefs.getString(KEY_LAST_DATE, "")
        
        if (lastDate != today) {
            // 新的一天，重置计数
            prefs.edit().apply {
                putInt(KEY_TODAY_COUNT, 0)
                putString(KEY_LAST_DATE, today)
                apply()
            }
            return 0
        }
        
        return prefs.getInt(KEY_TODAY_COUNT, 0)
    }
    
    /**
     * 增加喝水记录
     */
    fun addWaterRecord(): Int {
        val count = getTodayCount() + 1
        prefs.edit().putInt(KEY_TODAY_COUNT, count).apply()
        
        // 同时保存到历史记录（用于统计）
        saveToHistory(count)
        
        return count
    }
    
    /**
     * 撤销喝水记录
     */
    fun undoWaterRecord(): Int {
        val count = getTodayCount()
        if (count <= 0) return 0
        
        val newCount = count - 1
        prefs.edit().putInt(KEY_TODAY_COUNT, newCount).apply()
        
        // 同时更新历史记录
        updateHistory(newCount)
        
        return newCount
    }
    
    /**
     * 更新历史记录（用于撤销操作）
     */
    private fun updateHistory(count: Int) {
        saveToHistory(count)
    }
    
    /**
     * 保存到历史记录
     */
    private fun saveToHistory(count: Int) {
        val history = getHistory(7) // 获取最近 7 天
        val today = getTodayString()
        
        // 更新或添加今日记录
        val existingIndex = history.indexOfFirst { it.date == today }
        val record = WaterRecord(today, count, System.currentTimeMillis())
        
        val updatedHistory = if (existingIndex >= 0) {
            history.toMutableList().apply {
                set(existingIndex, record)
            }
        } else {
            history.toMutableList().apply {
                add(record)
            }
        }
        
        saveHistory(updatedHistory)
    }
    
    /**
     * 获取历史记录
     */
    fun getHistory(days: Int = 7): List<WaterRecord> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        
        val type = object : TypeToken<List<WaterRecord>>() {}.type
        val history: List<WaterRecord> = gson.fromJson(json, type)
        
        // 返回最近 N 天的记录
        return history.sortedByDescending { it.date }.take(days)
    }
    
    /**
     * 保存历史记录
     */
    private fun saveHistory(history: List<WaterRecord>) {
        val json = gson.toJson(history)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }
    
    /**
     * 获取每日目标
     */
    fun getDailyGoal(): DailyGoal {
        val json = prefs.getString(KEY_DAILY_GOAL, null)
        return json?.let {
            gson.fromJson(it, DailyGoal::class.java)
        } ?: DailyGoal()
    }
    
    /**
     * 保存每日目标
     */
    fun saveDailyGoal(goal: DailyGoal) {
        val json = gson.toJson(goal)
        prefs.edit().putString(KEY_DAILY_GOAL, json).apply()
    }
    
    /**
     * 获取杯子容量（ml）
     */
    fun getCupSize(): Int {
        return prefs.getInt(KEY_CUP_SIZE, DEFAULT_CUP_SIZE)
    }
    
    /**
     * 保存杯子容量
     */
    fun setCupSize(sizeMl: Int) {
        prefs.edit().putInt(KEY_CUP_SIZE, sizeMl).apply()
    }
    
    /**
     * 获取今日日期字符串
     */
    private fun getTodayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    
    /**
     * 获取格式化日期（用于图表显示）
     */
    fun getFormattedDate(date: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val output = SimpleDateFormat("MM/dd", Locale.getDefault())
            output.format(sdf.parse(date)!!)
        } catch (e: Exception) {
            date
        }
    }
}
