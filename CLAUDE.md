# NotifyEnh — Android 通知增强工具

## 项目概述

Android 通知增强工具，监听系统通知，按规则自动处理。纯本地，无网络权限。

---

## 技术栈

| 维度 | 选型 | 版本 |
|------|------|------|
| 语言 | Kotlin | 2.3.21 |
| UI | Jetpack Compose + Material 3 | BOM 2026.05.01 |
| 数据库 | Room (KSP) | 2.8.4 |
| 配置存储 | Jetpack DataStore (Preferences) | 1.2.1 |
| 分页 | Paging 3 Runtime + Compose | 3.5.0 |
| 构建 | Gradle Kotlin DSL, AGP 9.2.1 |
| 序列化 | kotlinx-serialization-json | 1.11.0 |
| 编译 SDK | 36 (minorApiLevel=1) |
| 最低 SDK | 30 |
| 目标 SDK | 36 |
| 其他依赖 | compose-markdown (0.5.6), kotlinx-collections-immutable (0.3.8) |

---

## 项目结构

```
F:/git/github/NotifyEnh.android/
├── build.gradle.kts              # 根 build 文件 (plugin 声明)
├── settings.gradle.kts           # 仓库配置 + jitpack 源
├── gradle.properties             # JVM args, Kotlin code style
├── gradle/
│   └── libs.versions.toml        # 版本目录 (所有依赖版本在此)
└── app/
    ├── build.gradle.kts          # 应用模块构建 (versionCode/Name 自动生成)
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/                  # 资源: drawable, values, mipmap, xml
        └── java/com/dansheng/notifyenh/
```

### 包结构 `com.dansheng.notifyenh`

```
App.kt                              # Application 类 (初始化 DataStore)
MainActivity.kt                     # 主入口 (Compose + 权限检查 + 底部导航)
data/
├── AppDatabase.kt                  # Room DB (通知/任务/日志 三表)
├── DbMigrations.kt                 # DB 迁移
├── NotificationEntity.kt           # 通知记录实体
├── NotificationDao.kt              # 通知记录 DAO (含 Paging)
├── TaskEntity.kt                   # 任务规则实体
├── TaskDao.kt                      # 任务规则 DAO
├── LogEntity.kt                    # 日志实体
├── LogDao.kt                       # 日志 DAO
└── prefs/
    └── AppPreferences.kt           # DataStore 偏好 (TTS/保留天数/上一次版本)

service/
├── NotifyEnhService.kt             # NotificationListenerService 核心
│   ├── onNotificationPosted → 匹配规则 → 取消/TTS/Alarm
│   ├── TTS 播报 (自带/系统引擎)
│   └── 允许 Alarm 任务 (pending intent → AlarmActivity)
└── BootReceiver.kt                 # 开机自启 → startService

ui/
├── screens/
│   ├── NotificationListScreen.kt   # 通知记录列表 (Paging, 搜索)
│   ├── TaskerScreen.kt             # 任务规则列表 + 增删改
│   ├── TaskEditDialog.kt           # 任务编辑弹窗 (匹配/动作配置)
│   └── SettingsScreen.kt           # 设置页 (TTS/保留天数/备份/权限)
├── components/
│   ├── ChangelogDialog.kt          # 更新日志弹窗 (Markdown)
│   └── LogDialog.kt                # 日志查看弹窗
├── theme/
│   ├── Color.kt                    # 颜色定义
│   ├── Theme.kt                    # Material 3 主题 (亮/暗)
│   └── Type.kt                     # 字体排版
├── AlarmActivity.kt                # 持续响铃全屏 Activity (showWhenLocked)
└── CrashActivity.kt                # 全局崩溃展示

util/
├── TTS.kt                          # 文字转语音 (系统 TTS / 自带引擎)
├── AlarmUtils.kt                   # 闹钟管理 (通知→PendingIntent→AlarmActivity)
├── BackupUtils.kt                  # 任务规则 JSON 导出/导入
├── CrashHandler.kt                 # 全局未捕获异常处理
└── LogUtils.kt                     # 内部日志写入 (File + Room)
```

---

## 权限清单

| 权限 | 用途 |
|------|------|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | 通知监听核心 |
| `POST_NOTIFICATIONS` | 应用内通知 |
| `RECEIVE_BOOT_COMPLETED` | 开机自启 |
| `FOREGROUND_SERVICE` + `SPECIAL_USE` | 前台保活 |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | 省电策略豁免 |
| `WAKE_LOCK` | 响铃唤醒 |
| `VIBRATE` | 振动 |
| `USE_FULL_SCREEN_INTENT` | 全屏提醒 |
| `QUERY_ALL_PACKAGES` | 通知来源应用名显示 |

---

## 构建与版本

- **versionCode**: `git rev-list --count HEAD`（提交次数）
- **versionName**: `SimpleDateFormat("yy.MM.dd")`（构建日期）
- **APK 命名**: `NotifyEnh_v{versionName}_{variant}.apk`
- **插件**: Android Application, Kotlin Compose, KSP, Kotlin Serialization

## 核心功能逻辑

### 通知处理流程 (`NotifyEnhService`)
```
onNotificationPosted
  → build NotificationEntity (包名/标题/内容/时间)
  → DB 写入通知记录
  → 遍历所有 TaskEntity 规则
    └─ 匹配 (关键词 或 正则) → 包名/标题/内容 三个维度
      ├─ auto_cancel → NotificationManager.cancel
      ├─ tts         → TTS.speak (系统引擎 / 自带离线引擎)
      └─ alarm       → AlarmUtils → PendingIntent → AlarmActivity
                           (持续响铃 + 振动 + 全屏)
```

### 任务规则字段
- 名称, 启用状态
- 匹配维度: 包名 / 标题 / 内容
- 匹配方式: 关键词 (contains) / 正则
- 动作: auto_cancel / tts / alarm (可多选)
- alarm 设置: 铃声 URI, 重复间隔

### 保留天数
可配: 1 / 3 / 7 / 30 天. AutoCleanWorker 定期清理过期通知记录.

---

## 值得注意的实现细节

1. **TTS 双引擎**: 优先系统 TextToSpeech; 自带离线引擎作为 fallback (引擎类 `TTSUtility`)
2. **Alarm 防重复**: 每个任务每次触发只设一个 alarm, PendingIntent 用 request code 区分
3. **崩溃处理**: `CrashHandler` 拦截未捕获异常 → 写入 Room Log → 启动 `CrashActivity` 展示
4. **DataStore**: 仅存轻量配置 (TTS 启用/保留天数/上次运行版本); 通知/任务/日志全走 Room
5. **Material 3 自适应**: 使用 `material3-adaptive-navigation-suite` 支持响应式布局
6. **Markdown 更新日志**: `compose-markdown` 渲染库内 CHANGELOG.md

---

## 开发环境

- Android Studio Ladybug+
- JDK 17+
- Android SDK 30+
- Gradle JVM: `-Xmx2048m`
