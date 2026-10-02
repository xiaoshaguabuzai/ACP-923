package com.acp923.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.acp923.shared.ui.App

/**
 * Web (wasmJs) 入口 —— 只做挂载点接线。
 *
 * - `viewportContainerId` 必须与 `webApp/src/main/resources/index.html` 里的容器 id 一致；
 * - JS 产物名在 build.gradle.kts 的 `outputFileName` 配置，需与 index.html 的 <script src> 同步；
 * - 开发：`gradlew.bat :webApp:wasmJsBrowserDevelopmentRun`（自动开浏览器）
 *   产物：`gradlew.bat :webApp:wasmJsBrowserDistribution`（静态文件，可丢给任意静态托管）
 * - 该入口只在浏览器环境可用；Web 侧没有系统级 API，涉及平台能力要走 expect/actual 的 web 分支。
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "composeTarget") {
        App()
    }
}
