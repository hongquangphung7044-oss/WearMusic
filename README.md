# WearMusic - Wear OS 音乐播放器

一款专为 **Wear OS (Galaxy Watch 7)** 打造的音乐播放器，支持插件导入、在线/离线播放、歌词显示与封面展示。

## 技术栈

| 模块 | 技术 |
|---|---|
| UI | Jetpack Compose for Wear OS |
| 播放 | Media3 ExoPlayer |
| 架构 | MVVM + Repository + Hilt DI |
| 网络 | Retrofit + Kotlin Serialization |
| 下载 | WorkManager |
| 构建 | Gradle + GitHub Actions CI |

## 功能

- **插件系统**：类似 MusicFree，可导入多种音乐源插件
- **在线/离线播放**：支持播放队列管理
- **封面 + 歌词**：下载时自动打包歌曲、封面和歌词
- **后台播放**：锁屏/熄屏时继续播放
- **媒体通知**：系统通知栏控制
- **32位 ARM**：专为 Wear OS 设备优化

## 项目结构

```
WearMusic/
├── app/src/main/java/com/example/wearmusic/
│   ├── data/         # 数据层 (Model, Repository, API)
│   ├── player/       # 播放器 ViewModel
│   ├── plugin/       # 插件接口与管理
│   ├── download/     # 下载 Worker
│   ├── service/      # 后台播放服务
│   ├── ui/           # Wear OS UI (Compose)
│   └── di/           # Hilt 依赖注入
├── .github/workflows/# CI 构建配置
└── README.md
```

## 快速开始

1. 克隆项目
2. 使用 Android Studio 打开
3. 连接 Wear OS 设备或启动模拟器
4. 点击 **Run** 或执行 `./gradlew installDebug`

## GitHub Actions 构建

项目配置了自动构建：
- **Push 到 main/develop**：自动构建并上传 Debug APK
- **Release 版本**：构建 Release APK

构建产物可在 Actions 页面下载。

## 插件开发

插件需实现 `MusicPlugin` 接口：

```kotlin
interface MusicPlugin {
    val pluginId: String
    val pluginName: String
    val version: String
    
    suspend fun search(keyword: String, page: Int, limit: Int): SearchResult
    suspend fun getSongDetail(song: Song): Song
    suspend fun getLyrics(song: Song): String
}
```

## 致谢

- 灵感来源：[MusicFree](https://github.com/maotoumao/MusicFree)
- 图标库：[Material Icons for Compose](https://fonts.google.com/icons)
