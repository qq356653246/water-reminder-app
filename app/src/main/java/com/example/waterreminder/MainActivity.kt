package com.example.waterreminder

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.waterreminder.data.DailyGoal
import com.example.waterreminder.data.WaterDataManager
import com.example.waterreminder.data.WaterRecord
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    // UI 组件
    private lateinit var countText: TextView
    private lateinit var nextTimeText: TextView
    private lateinit var goalText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var drinkButton: MaterialButton
    private lateinit var undoButton: MaterialButton
    private lateinit var settingsButton: MaterialButton
    private lateinit var barChart: BarChart
    
    // 数据管理器
    private lateinit var dataManager: WaterDataManager
    
    // 设置相关
    private var dailyGoal = 8
    private var reminderIntervalMinutes = 60
    private var startTime = "08:00"
    private var endTime = "22:00"

    companion object {
        const val CHANNEL_ID = "water_reminder_channel"
        const val REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化数据管理器
        dataManager = WaterDataManager(this)

        // 初始化视图
        initViews()

        // 创建通知渠道
        createNotificationChannel()

        // 请求通知权限
        requestNotificationPermission()

        // 加载设置
        loadSettings()

        // 更新界面
        updateUI()

        // 设置提醒
        scheduleReminder()

        // 设置按钮点击事件
        setupClickListeners()

        // 初始化图表
        initChart()
    }

    private fun initViews() {
        countText = findViewById(R.id.countText)
        nextTimeText = findViewById(R.id.nextTimeText)
        goalText = findViewById(R.id.goalText)
        progressBar = findViewById(R.id.progressBar)
        drinkButton = findViewById(R.id.drinkButton)
        undoButton = findViewById(R.id.undoButton)
        settingsButton = findViewById(R.id.settingsButton)
        barChart = findViewById(R.id.barChart)
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

    private fun loadSettings() {
        val goal = dataManager.getDailyGoal()
        dailyGoal = goal.targetCups
        startTime = goal.startTime
        endTime = goal.endTime
        
        // 加载提醒间隔（从 SharedPreferences）
        val prefs = getSharedPreferences("water_prefs", Context.MODE_PRIVATE)
        reminderIntervalMinutes = prefs.getInt("interval_minutes", 60)
    }

    private fun updateUI() {
        val count = dataManager.getTodayCount()
        countText.text = count.toString()
        
        // 更新进度条
        progressBar.progress = count.coerceAtMost(dailyGoal)
        goalText.text = "目标：$dailyGoal 杯"
        
        // 计算下次提醒时间
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, reminderIntervalMinutes)
        }
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        nextTimeText.text = timeFormat.format(calendar.time)
        
        // 更新图表
        updateChart()
    }

    private fun setupClickListeners() {
        drinkButton.setOnClickListener {
            drinkWater()
        }

        undoButton.setOnClickListener {
            undoDrinkWater()
        }

        settingsButton.setOnClickListener {
            showSettingsDialog()
        }
    }

    private fun drinkWater() {
        val count = dataManager.addWaterRecord()
        
        countText.text = count.toString()
        progressBar.progress = count.coerceAtMost(dailyGoal)
        
        // 动画反馈
        countText.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(150)
            .withEndAction {
                countText.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(150)
                    .start()
            }
            .start()
        
        Toast.makeText(this, "已记录一杯水！💧", Toast.LENGTH_SHORT).show()
        
        // 重新设置提醒
        scheduleReminder()
        
        // 更新图表
        updateChart()
        
        // 检查是否完成目标
        if (count == dailyGoal) {
            Toast.makeText(this, "🎉 恭喜完成今日目标！", Toast.LENGTH_LONG).show()
        }
    }

    private fun undoDrinkWater() {
        val currentCount = dataManager.getTodayCount()
        
        if (currentCount <= 0) {
            Toast.makeText(this, "已经是 0 杯了，无法撤销～", Toast.LENGTH_SHORT).show()
            return
        }
        
        val newCount = dataManager.undoWaterRecord()
        
        countText.text = newCount.toString()
        progressBar.progress = newCount.coerceAtMost(dailyGoal)
        
        Toast.makeText(this, "已撤销一杯，当前 $newCount 杯", Toast.LENGTH_SHORT).show()
        
        // 更新图表
        updateChart()
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

        // 计算下次提醒时间（考虑时间段）
        val triggerTime = calculateNextReminderTime()

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

    private fun calculateNextReminderTime(): Long {
        val now = Calendar.getInstance()
        val startTimeParts = startTime.split(":")
        val endTimeParts = endTime.split(":")
        
        val startHour = startTimeParts[0].toInt()
        val startMinute = startTimeParts[1].toInt()
        val endHour = endTimeParts[0].toInt()
        val endMinute = endTimeParts[1].toInt()
        
        // 如果当前时间不在时间段内，则设置为明天开始时间
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        val currentTime = currentHour * 60 + currentMinute
        val startTotalMinutes = startHour * 60 + startMinute
        val endTotalMinutes = endHour * 60 + endMinute
        
        if (currentTime < startTotalMinutes) {
            // 当前时间在开始时间之前，设置为今天开始时间
            now.set(Calendar.HOUR_OF_DAY, startHour)
            now.set(Calendar.MINUTE, startMinute)
        } else if (currentTime >= endTotalMinutes) {
            // 当前时间在结束时间之后，设置为明天开始时间
            now.add(Calendar.DAY_OF_YEAR, 1)
            now.set(Calendar.HOUR_OF_DAY, startHour)
            now.set(Calendar.MINUTE, startMinute)
        } else {
            // 在时间段内，正常计算下次提醒
            now.add(Calendar.MINUTE, reminderIntervalMinutes)
            
            // 如果超过结束时间，则设置为明天开始时间
            val nextTime = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            if (nextTime > endTotalMinutes) {
                now.add(Calendar.DAY_OF_YEAR, 1)
                now.set(Calendar.HOUR_OF_DAY, startHour)
                now.set(Calendar.MINUTE, startMinute)
            }
        }
        
        return now.timeInMillis
    }

    private fun showSettingsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null)
        
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
        
        val dialog = builder.create()
        dialog.show()
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // 获取视图组件
        val txtGoalCups = dialogView.findViewById<TextView>(R.id.txtGoalCups)
        val btnIncreaseGoal = dialogView.findViewById<Button>(R.id.btnIncreaseGoal)
        val btnDecreaseGoal = dialogView.findViewById<Button>(R.id.btnDecreaseGoal)
        val spinnerInterval = dialogView.findViewById<Spinner>(R.id.spinnerInterval)
        val btnStartTime = dialogView.findViewById<Button>(R.id.btnStartTime)
        val btnEndTime = dialogView.findViewById<Button>(R.id.btnEndTime)
        val btnSaveSettings = dialogView.findViewById<MaterialButton>(R.id.btnSaveSettings)
        
        // 初始化设置值
        txtGoalCups.text = dailyGoal.toString()
        btnStartTime.text = startTime
        btnEndTime.text = endTime
        
        // 设置提醒间隔选项
        val intervals = arrayOf("30 分钟", "60 分钟", "90 分钟", "120 分钟")
        val intervalValues = arrayOf(30, 60, 90, 120)
        val currentIndex = intervalValues.indexOf(reminderIntervalMinutes)
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, intervals)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerInterval.adapter = adapter
        spinnerInterval.setSelection(currentIndex)
        
        // 增加目标
        btnIncreaseGoal.setOnClickListener {
            if (dailyGoal < 20) {
                dailyGoal++
                txtGoalCups.text = dailyGoal.toString()
            }
        }
        
        // 减少目标
        btnDecreaseGoal.setOnClickListener {
            if (dailyGoal > 1) {
                dailyGoal--
                txtGoalCups.text = dailyGoal.toString()
            }
        }
        
        // 选择开始时间
        btnStartTime.setOnClickListener {
            val timeParts = startTime.split(":")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()
            
            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                startTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                btnStartTime.text = startTime
            }, hour, minute, true).show()
        }
        
        // 选择结束时间
        btnEndTime.setOnClickListener {
            val timeParts = endTime.split(":")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()
            
            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                endTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                btnEndTime.text = endTime
            }, hour, minute, true).show()
        }
        
        // 保存设置
        btnSaveSettings.setOnClickListener {
            // 保存每日目标
            val goal = DailyGoal(dailyGoal, startTime, endTime)
            dataManager.saveDailyGoal(goal)
            
            // 保存提醒间隔
            val selectedInterval = intervalValues[spinnerInterval.selectedItemPosition]
            val prefs = getSharedPreferences("water_prefs", Context.MODE_PRIVATE)
            prefs.edit().putInt("interval_minutes", selectedInterval).apply()
            reminderIntervalMinutes = selectedInterval
            
            // 重新设置提醒
            scheduleReminder()
            updateUI()
            
            Toast.makeText(this, "设置已保存！", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun initChart() {
        // 图表基本设置
        barChart.apply {
            description.isEnabled = false
            setPinchZoom(false)
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            legend.isEnabled = false
            
            // X 轴设置
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }
            
            // Y 轴设置
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E0E0E0")
                axisMinimum = 0f
            }
            
            axisRight.isEnabled = false
        }
    }

    private fun updateChart() {
        val history = dataManager.getHistory(7)
        
        if (history.isEmpty()) {
            barChart.clear()
            barChart.invalidate()
            return
        }
        
        // 准备数据
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        
        // 反转列表，使最近的日期在右边
        val reversedHistory = history.reversed()
        
        reversedHistory.forEachIndexed { index, record ->
            entries.add(BarEntry(index.toFloat(), record.count.toFloat()))
            labels.add(dataManager.getFormattedDate(record.date))
        }
        
        // 创建数据集
        val dataSet = BarDataSet(entries, "喝水杯数").apply {
            colors = listOf(
                Color.parseColor("#2196F3"),
                Color.parseColor("#4FC3F7"),
                Color.parseColor("#81D4FA"),
                Color.parseColor("#B3E5FC"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#4FC3F7"),
                Color.parseColor("#81D4FA")
            )
            valueTextColor = Color.parseColor("#757575")
            valueTextSize = 12f
        }
        
        // 设置数据
        val barData = BarData(dataSet).apply {
            barWidth = 0.6f
        }
        
        barChart.apply {
            data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = labels.size
            fitScreen()
            invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
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
