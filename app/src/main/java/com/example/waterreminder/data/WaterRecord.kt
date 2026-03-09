package com.example.waterreminder.data

/**
 * 喝水记录数据类
 */
data class WaterRecord(
    val date: String,      // 日期 yyyy-MM-dd
    val count: Int,        // 杯数
    val timestamp: Long    // 时间戳
)

/**
 * 每日喝水目标
 */
data class DailyGoal(
    val targetCups: Int = 8,           // 目标杯数
    val startTime: String = "08:00",   // 开始时间
    val endTime: String = "22:00"      // 结束时间
)
