// Web (Wasm) 应用模块 —— 壳，只做挂载点接线。
//
// 为什么用 wasmJs 而不是 js：Compose for Web 的新产物走 Wasm（性能与包体积都更好），
// 老项目的 JS(IR) 目标已在模板里移除；确需兼容老浏览器时再加 `js { browser() }`。
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        binaries.executable()
        browser {
            commonWebpackConfig {
                // ⚠️ 改这里要同步改 src/main/resources/index.html 的 <script src>
                outputFileName = "acp923-app.js"

                devServer = devServer?.copy(
                    open = true,
                    port = 8080
                )
            }
        }
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":shared"))
            // CanvasBasedWindow / ComposeViewport 所在的跨平台 UI 产物
            implementation(libs.compose.ui)
        }
    }
}
