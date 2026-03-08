# 💧 喝水提醒 App

一个简单的 Android 喝水提醒应用，定时提醒你喝水保持健康。

## 功能

- ⏰ 定时提醒（可设置 30/60/90/120 分钟）
- 📊 记录每天喝水杯数
- 🔔 通知栏提醒
- 🔄 开机自动启动
- 📱 简洁 Material Design 界面

## 项目结构

```
water-reminder-app/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/waterreminder/
│   │   │   ├── MainActivity.kt          # 主界面
│   │   │   ├── WaterReminderReceiver.kt # 提醒广播接收器
│   │   │   └── BootReceiver.kt          # 开机启动接收器
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml    # 主界面布局
│   │   │   └── values/
│   │   │       ├── strings.xml          # 字符串资源
│   │   │       ├── colors.xml           # 颜色定义
│   │   │       └── themes.xml           # 主题样式
│   │   └── AndroidManifest.xml          # 应用清单
│   ├── build.gradle.kts                 # App 构建配置
│   └── proguard-rules.pro               # 混淆规则
├── build.gradle.kts                     # 项目构建配置
├── settings.gradle.kts                  # 项目设置
└── gradle.properties                    # Gradle 属性
```

## 使用方法

### 1. 打开项目

在 Android Studio 中：
```
File → Open → 选择 water-reminder-app 文件夹
```

### 2. 等待 Gradle 同步

第一次打开会自动下载依赖，可能需要几分钟。

### 3. 运行应用

- 连接 Android 手机（开启 USB 调试）
- 或创建模拟器
- 点击运行按钮（绿色三角形）

### 4. 生成 APK

```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

APK 位置：`app/build/outputs/apk/debug/app-debug.apk`

## 权限说明

- `POST_NOTIFICATIONS` - 发送通知（Android 13+ 需要）
- `SCHEDULE_EXACT_ALARM` - 精确闹钟
- `RECEIVE_BOOT_COMPLETED` - 开机启动
- `VIBRATE` - 震动提醒

## 自定义

### 修改默认提醒间隔

编辑 `MainActivity.kt`：
```kotlin
private var reminderIntervalMinutes = 60 // 改为其他分钟数
```

### 修改应用名称

编辑 `res/values/strings.xml`：
```xml
<string name="app_name">你的应用名</string>
```

### 修改主题色

编辑 `res/values/colors.xml`：
```xml
<color name="blue_water">#你的颜色代码</color>
```

## 技术栈

- **语言**: Kotlin
- **最低版本**: Android 7.0 (API 24)
- **目标版本**: Android 14 (API 34)
- **UI**: Material Design 3
- **架构**: 传统 Activity + SharedPreferences

## 下一步优化建议

- [ ] 添加桌面小部件
- [ ] 添加喝水历史记录图表
- [ ] 添加自定义提醒时间（如 8:00-22:00）
- [ ] 添加数据备份/恢复
- [ ] 添加多种提醒音效
- [ ] 适配深色模式

## License

MIT License - 随便用！
