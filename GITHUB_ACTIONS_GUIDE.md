# 🚀 GitHub Actions 编译指南

## 快速开始

### 1️⃣ 创建 GitHub 仓库

1. 登录 GitHub (https://github.com)
2. 点击右上角 **+** → **New repository**
3. 仓库名：`water-reminder-app`
4. 设为 **Public** 或 **Private** 都可以
5. 点击 **Create repository**

### 2️⃣ 推送代码到 GitHub

```bash
cd /home/admin/.openclaw/workspace/water-reminder-app

# 初始化 git（如果还没有）
git init

# 添加所有文件
git add .

# 提交
git commit -m "Initial commit - Water Reminder App"

# 添加远程仓库（替换为你的仓库地址）
git remote add origin https://github.com/你的用户名/water-reminder-app.git

# 推送
git branch -M main
git push -u origin main
```

### 3️⃣ 等待自动编译

推送后，GitHub Actions 会自动开始编译：

1. 进入你的仓库页面
2. 点击 **Actions** 标签
3. 看到 "Android CI" 工作流正在运行
4. 等待约 3-5 分钟

### 4️⃣ 下载 APK

编译成功后：

1. 在 Actions 页面点击绿色的 ✅ 成功记录
2. 滚动到页面底部 **Artifacts** 部分
3. 点击 **app-debug** 下载 APK
4. 解压后得到 `app-debug.apk`

---

## 🔧 手动触发编译

如果需要重新编译（比如修改了代码）：

1. 进入仓库 **Actions** 标签
2. 点击左侧 **Android CI**
3. 点击右上角 **Run workflow**
4. 选择分支（main）
5. 点击 **Run workflow**

---

## 📱 安装 APK

下载 APK 后安装到手机：

1. 传输 APK 到手机
2. 设置 → 安全 → 允许"未知来源"应用
3. 点击 APK 安装

---

## ⚙️ 配置说明

### 编译环境
- **系统**: Ubuntu Latest
- **JDK**: 17
- **内存**: 7GB (GitHub 免费 runner)
- **Gradle**: 自动缓存加速

### 输出
- **APK 路径**: `app/build/outputs/apk/debug/app-debug.apk`
- **保留时间**: 30 天

---

## 🐛 如果编译失败

1. 点击失败的 workflow 记录
2. 展开日志查看错误
3. 常见错误：
   - `gradlew not found` → 确保 gradlew 文件已提交
   - `SDK not found` → 自动配置，一般没问题
   - `Out of memory` → GitHub 会提供足够内存

---

## 📝 后续更新代码

```bash
# 修改代码后
git add .
git commit -m "修复闪退问题"
git push
```

Actions 会自动重新编译！

---

**有问题随时问！** 🙌
