package com.acp923.shared

/**
 * 平台差异的 **expect 声明**。
 *
 * ## 使用规则（架构约定）
 * - 只有「各平台实现真的不同」的能力才走 expect/actual（引擎选择、系统 API 封装、
 *   文件路径、通知、生物识别等）；纯业务逻辑不要下沉到 actual，否则测试成本翻倍。
 * - 平台能表达为有限集合时，用 `enum` 收敛，别把字符串散落到业务代码里比较。
 * - 一个 expect 声明在 commonMain 只允许有一个 actual（同名同包）。
 *
 * ## 扩展范式
 * ```
 * expect class PlatformStorage { fun read(key: String): String? }   // 能力型
 * expect fun currentAppVersion(): String                            // 值型
 * ```
 */
enum class Platform { ANDROID, IOS, DESKTOP, WEB }

/** 编译期确定的当前平台。各源集在 `Platform.<平台>.kt` 里给出 actual。 */
expect val currentPlatform: Platform

/** UI 展示用的平台名。放在 commonMain 保证四个平台文案一致。 */
val Platform.displayName: String
    get() = when (this) {
        Platform.ANDROID -> "Android"
        Platform.IOS -> "iOS"
        Platform.DESKTOP -> "Desktop (JVM)"
        Platform.WEB -> "Web (Wasm)"
    }
