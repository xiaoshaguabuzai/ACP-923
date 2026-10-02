# ACP-923 · KMP 项目模板

Kotlin Multiplatform + Compose Multiplatform 起手模板。
**Android / iOS / Desktop / Web(Wasm) 四平台的编译链路都已在本机验证通过。**

> 架构约定、模块拆分时机、依赖注入/网络接入范式 → 见 **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)**
> iOS 壳工程（Xcode）配置 → 见 **[iosApp/README.md](iosApp/README.md)**

## 快速开始

```powershell
# 本机没有系统级 java，先指到已装的 JDK 21
$env:JAVA_HOME = "C:\Users\Administrator\.jdks\ms-21.0.12.1"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

.\gradlew.bat :androidApp:installDebug          # Android 设备/模拟器
.\gradlew.bat :desktopApp:run                   # 桌面窗口
.\gradlew.bat :webApp:wasmJsBrowserDevelopmentRun   # 浏览器
.\gradlew.bat desktopTest                        # 最快反馈回路（commonTest）
.\gradlew.bat ktlintCheck                        # 风格门禁
```

> Git Bash 下 `./gradlew` 不可用（缺 `cygpath`），一律用 PowerShell 执行 `gradlew.bat`。

## 结构

```
ACP-923/
├── build-logic/convention/        # 约定插件（included build）
│   ├── acp.kmp.library            # KMP 共享模块：四平台目标 + 按开关注入依赖 + ktlint
│   └── acp.android.app            # Android 应用壳：SDK 级别 + Compose + ktlint
├── gradle/libs.versions.toml      # 唯一版本事实来源
├── shared/                        # 共享 UI + 状态 + 逻辑（ui / presentation 分层）
├── androidApp/  desktopApp/  webApp/   # 三个壳：只做平台接线
├── iosApp/                        # iOS 壳（Swift 源码 + Xcode 配置说明）
└── docs/ARCHITECTURE.md           # 架构契约与演进路径
```

## 锁定版本

| 组件 | 版本 | 组件 | 版本 |
|---|---|---|---|
| Gradle | 9.6.1 | Ktor Client | 3.5.1 |
| AGP | 9.1.0 | Koin | 4.2.2 |
| Kotlin | 2.4.10 | Coil | 3.6.0 |
| Compose Multiplatform | 1.11.1 | coroutines / serialization / datetime | 1.11.0 / 1.11.0 / 0.8.0 |
| material3（独立版本线） | 1.12.0-alpha03 | multiplatform-settings | 1.3.0 |
| compileSdk / minSdk / targetSdk | 37 / 24 / 37 | lifecycle / navigation (KMP) | 2.11.0-beta01 / 2.9.0 |

## 依赖开关（共享模块）

```kotlin
plugins { alias(libs.plugins.acpKmpLibrary) }

acp {
    network = false    // 不用网络就不引入 Ktor（含各平台引擎）
    di = false         // 不用 Koin
    images = false     // 不用 Coil
    storage = false    // 不用 KV 存储
}
```

## 拿它开新项目（checklist）

1. 复制整个目录 → 新项目名（`build/`、`.gradle/` 已 gitignore，不会带过去）。
2. `settings.gradle.kts`：`rootProject.name`，按需增删 `include(":…")`。
3. 全局替换包名 `com.acp923` → 你的包名（含 build-logic 里 `KotlinAndroid.kt` 的推导前缀）。
4. `androidApp/build.gradle.kts`：`namespace` / `applicationId` / `versionCode` / `versionName`。
5. 名称残留：桌面窗口标题、`webApp` 的 `outputFileName` 与 `index.html`、iOS `baseName`（`shared/build.gradle.kts`）。
6. `shared` 里删掉示例（`CounterViewModel`、示例字符串资源），替换 `ui/App.kt` 内容。
7. 四平台验证：`assembleDebug` + `compileKotlin` + `compileKotlinWasmJs` + `compileKotlinIosSimulatorArm64`。

## 本机网络注意事项（已内置，别随意改仓库顺序）

- **依赖**：`google()` / `mavenCentral()` 在前，阿里云镜像兜底。镜像放前面会导致
  KMP 的 `*-metadata.jar` 404 负缓存（iOS/native 的 `*ForIde` 任务集体失败）；
  中招后跑 `.\gradlew.bat --refresh-dependencies`。
- **插件**：阿里云 `gradle-plugin` 镜像必须在 `gradlePluginPortal()` **之前**
  （本机访问 portal 会 Connection reset，且会中断整条仓库链）。
- `build-logic` 是独立 included build，**不继承**根项目仓库配置，需在其
  `settings.gradle.kts` 内单独配置。

## 其他提示

- **热重载（Desktop）**：装 JBR 21 到 `~/.jdks/jbr-21`，取消 `gradle.properties` 里
  `compose.reload.jbr.binary` 的注释。
- **CI 严格模式**：`gradlew.bat -PwarningsAsErrors=true build`。
- **越界警告**：`Could not find compile target android-37` → SDK 的
  `platforms\android-37` Junction 丢了，重建指向 `android-37.0` 即可。
