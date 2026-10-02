# ACP-923 架构说明

> 读者：接手这个模板的人（含三个月后的自己）。目标是把"为什么这么搭"讲清楚，
> 以及**什么时候该演进**、往哪儿演进。

## 1. 设计目标与取舍

| 目标 | 做法 | 代价（有意接受） |
|---|---|---|
| 复制即跑 | 四平台最小可运行壳 + 全部平台编译校验通过 | 模板自带示例代码，新项目要先删 |
| 依赖集中 | 版本目录 + 约定插件双层收敛 | 改依赖要动 build-logic，不能只改模块 |
| 平台对齐 | Ktor 引擎、Compose skiko 等按源集分派 | 多一个"源集矩阵"要记 |
| 早期不复杂 | 单 `shared` 模块承载 UI + 逻辑 | 模块变多后需要按第 7 节演进 |

**明确的非目标**：不预置 DI 容器与网络层实现。理由：那属于"业务选型"，
模板给的是**已锁定版本的依赖 + 接入范式**（见第 6 节），避免模板一上来就埋半成品代码。

## 2. 模块与依赖方向

```
            ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
            │  androidApp  │  │  desktopApp  │  │    webApp    │   ← 壳：只做平台接线
            │ (Compose MP) │  │ (Compose MP) │  │  (wasmJs)    │
            └──────┬───────┘  └──────┬───────┘  └──────┬───────┘
                   │                 │                 │
                   └────────┬────────┴────────┬────────┘
                            ▼                 ▼
                       ┌────────────────────────────┐
                       │          shared            │   ← 共享：UI + 状态 + 逻辑
                       │  ui/ presentation/ ...     │
                       └────────────────────────────┘
                                    ▲
                       iosApp (SwiftUI + SharedKit Framework)
```

**依赖方向铁律**：`壳 → shared`，**永不反向**。
壳里不允许出现业务逻辑；shared 里不允许出现平台入口类型
（`Activity`、`Window`、`ComposeViewport`、`UIViewController`）。

壳模块的职责只有四件事：生命周期、窗口/容器、平台权限、把 `App()` 挂上去。

## 3. shared 内部分层

```
com.acp923.shared
├── Platform.kt                 # expect 声明 + Platform 枚举（跨平台能力边界）
├── ui/
│   ├── App.kt                  # 根 Composable：主题 + Scaffold + 导航占位
│   └── theme/Theme.kt          # 颜色/排版/形状唯一入口
├── presentation/
│   └── CounterViewModel.kt     # ViewModel + StateFlow 范式
└── （建议后续目录）
    ├── data/                   # 仓储、DTO、数据源
    ├── domain/                 # 用例、领域模型（纯 Kotlin，无 Compose/无平台 API）
    └── di/                     # Koin 模块定义
```

包内分层规则（比"文件放哪儿"更重要）：

- `domain` 层**只依赖 Kotlin 标准库与 kotlinx**：不碰 Compose、不碰平台 API，
  这样它天然可单测、可被 Serve 端复用；
- `presentation` 依赖 `domain`，`ui` 依赖 `presentation`；
- 反向依赖（如 `domain` 引用 `ui` 的类型）一律视为设计事故。

## 4. 约定插件契约

| 插件 id | 适用模块 | 它负责 | 调用方必须自己写 |
|---|---|---|---|
| `acp.kmp.library` | 共享模块（KMP 库） | 基础插件、四平台目标、按开关注入依赖、ktlint | `namespace`、SDK 级别、iOS `baseName`、业务依赖 |
| `acp.android.app` | Android 应用 | `com.android.application` + Compose + ktlint、SDK 级别、JVM 17、打包排除 | `applicationId`、`versionCode/Name`、签名与混淆 |

依赖开关（`acp { }`）的语义：

```kotlin
acp {
    network = true     // Ktor client + 各平台引擎
    di = true          // Koin core/compose/viewmodel
    images = true      // Coil 3
    storage = true     // multiplatform-settings
    navigation = true  // JetBrains navigation-compose
    lifecycle = true   // JetBrains lifecycle-viewmodel/runtime-compose
    datetime = true    // kotlinx-datetime
}
```

关掉即**不引入**（不是留着不用）—— 模块多起来后这直接决定构建时间与依赖面。
注意 `images` 依赖 `network`（Coil 用 Ktor 引擎），单独用 Coil 时两者都开。

## 5. 源集矩阵：依赖放哪一格

| 源集 | 典型内容 | 本模板已配 |
|---|---|---|
| `commonMain` | 全部共享逻辑、Compose UI、expect 声明 | Compose、协程、序列化、lifecycle、Ktor core |
| `androidMain` | Android 专属实现（Context、权限、OkHttp 引擎） | androidx-core-ktx、ktor-okhttp |
| `desktopMain` | JVM 桌面实现（skiko、Swing 调度器） | `compose.desktop.currentOs`、coroutines-swing、ktor-okhttp |
| `iosMain` | iOS 专属实现（Darwin 引擎、UIKit 桥） | ktor-darwin |
| `wasmJsMain` | Web 实现（JS 引擎、浏览器 API） | ktor-js |
| `commonTest` | 跨平台单测（每个平台各跑一遍） | kotlin-test、coroutines-test、ktor-client-mock |

**判断口诀**：能编译进 `commonMain` 的就不要下沉到平台源集。
只有在"同一份代码在各平台无法编译或需要不同实现"时才用 expect/actual 或平台源集。

## 6. 常见接入范式（复制即用）

### Koin（依赖注入）

```kotlin
// shared/src/commonMain/kotlin/.../di/AppModule.kt
val appModule = module {
    viewModelOf(::CounterViewModel)          // koin-compose-viewmodel
    single<HttpClient>(::createHttpClient)
}

// shared/src/commonMain/kotlin/.../di/Koin.kt —— 各平台入口统一调用
fun initKoin(config: KoinAppDeclaration? = null) =
    startKoin { config?.invoke(this); modules(appModule) }
```
Android 在 `MainActivity.onCreate` 前、Desktop 在 `main()` 里、iOS 在
`MainViewController()` 里各调一次 `initKoin()`。Koin 的 Compose 集成用
`KoinApplication { App() }` 包住根 Composable，ViewModel 用 `koinViewModel()` 取。

### Ktor Client（跨平台 HTTP）

```kotlin
// commonMain：只写"要什么"，不写"用什么引擎"
expect fun httpEngine(): HttpClientEngineFactory<*>

fun createHttpClient(engine: HttpClientEngineFactory<*>) = HttpClient(engine) {
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    install(Logging) { level = LogLevel.INFO }
}
```
引擎从 `androidMain/desktopMain`（OkHttp）、`iosMain`（Darwin）、`wasmJsMain`（Js）分别 actual。
**不要**在 commonMain 里 `HttpClient()` 空构造 —— 那会因缺少引擎在部分平台运行时崩。

### 数据持久化

- 键值对（设置、开关、token）：`multiplatform-settings`（已锁定 1.3.0）；
- 结构化数据：加 SQLDelight（版本需先在练习项目验证，见第 8 节）。

## 7. 模块拆分演进路径

单 `shared` 能撑到大约"1 个 App + 5~10 个页面"。出现下列信号就该拆：

| 信号 | 拆分动作 |
|---|---|
| 设计系统要复用到第二个 App | 抽 `core:designsystem`（搬走 `ui/theme`） |
| 网络/数据层被多模块共享 | 抽 `core:network`（Ktor client + 引擎 actual）、`core:data` |
| 页面数量 > 10，构建变慢 | 按功能抽 `feature:<name>`，每个 feature 用 `acp.kmp.library` |
| 出现"平台专属业务"（如仅 Android 的桌面小组件） | 该 feature 单独建模块，不要污染 commonMain |

拆分时的机械步骤：新建模块 → `plugins { alias(libs.plugins.acpKmpLibrary) }` →
配 `namespace` + iOS baseName → 从 `shared` 移文件与源集 → `settings.gradle.kts` 加 `include`。
**包名不要跟着模块改**（保持 `com.acp923.*`），否则 history 与外部引用会断层。

## 8. 版本与升级策略

- 单一事实来源：`gradle/libs.versions.toml`。模块与约定插件都只能通过 `libs.*` 访问。
- 升级纪律（本机验证过的顺序）：
  1. 先在练习项目（kotlinconf-app / Shopping-By-KMP）里单独升一个版本；
  2. 四平台任务全部通过：`:androidApp:assembleDebug`、`:desktopApp:compileKotlin`、
     `:webApp:compileKotlinWasmJs`、`:shared:compileKotlinIosSimulatorArm64`；
  3. 再改模板 toml；
  4. 若报 `Could not find *-metadata.jar`：`gradlew.bat --refresh-dependencies` 清负缓存。
- 同类版本要**成组升**：AGP ↔ Kotlin ↔ Compose MP ↔ Kotlin 编译器插件是耦合的；
  Koin 与 Ktor 也常成对（Koin 的 ktor 集成依赖 Ktor 版本）。

**已知需要留意的两条**（本机踩过）：

1. `org.jetbrains.compose.material3:material3` **不跟 Compose MP 同版本发布**，1.10 起只有 alpha。
   模板锁的是 CM 1.11.1 可用的 `1.12.0-alpha03`；升 CM 时它要单独验证。
2. AGP 9.0 起 `com.android.library` 与 KMP 互斥，必须用
   `com.android.kotlin.multiplatform.library`（模板已用；见 `AcpKmpLibraryConventionPlugin` 注释）。

## 9. 质量门禁

| 手段 | 位置 | 怎么用 |
|---|---|---|
| ktlint | 两个约定插件默认接入，规则读根 `.editorconfig` | `gradlew.bat ktlintCheck` / `ktlintFormat` |
| 警告即错误 | `KotlinAndroid.kt` 里的 `warningsAsErrors` | `gradlew.bat -PwarningsAsErrors=true build` |
| 跨平台单测 | `commonTest`（每平台各跑一遍） | `gradlew.bat desktopTest`（本机最快的反馈回路） |
| 配置缓存 | `gradle.properties` 已开 | 若报 cache 序列化错误，先看是否引了不支持缓存的插件 |

## 10. 平台能力边界（写代码前先查这张表）

| 能力 | Android | iOS | Desktop | Web(Wasm) |
|---|---|---|---|---|
| Compose UI | ✅ | ✅ | ✅ | ✅ |
| 协程 / Flow | ✅ | ✅ | ✅ | ✅ |
| Ktor 引擎 | OkHttp | Darwin | OkHttp | Js |
| 文件系统 | ✅ | ✅ | ✅ | ❌（无本地 FS） |
| 后台任务 | WorkManager | BGTask | 手动线程 | ❌ |
| 系统通知 | ✅ | ✅ | 部分（托盘） | ❌ |
| 生物识别 | ✅ | ✅ | ❌ | ❌ |

规律：**Web 是最窄的平台**。涉及 FS / 系统服务的能力，要么做成 expect/actual 并给 Web 降级实现，
要么明确标注"Web 不支持"。
