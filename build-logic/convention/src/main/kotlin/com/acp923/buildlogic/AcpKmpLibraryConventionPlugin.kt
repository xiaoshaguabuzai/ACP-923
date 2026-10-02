package com.acp923.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * 共享模块的能力开关（`acp { }` DSL）。
 *
 * 设计取舍：模板默认「全开」，让新模块拿来即用；但只依赖真正用到的东西才符合
 * 依赖最小化原则 —— 关掉某个开关后，对应依赖不会出现在任何源集的 classpath 上。
 *
 * 用法（模块 build.gradle.kts）：
 * ```
 * plugins { alias(libs.plugins.acpKmpLibrary) }
 *
 * acp {
 *     di = false        // 不用 Koin 就关掉
 *     images = false    // 不做图片加载
 * }
 * ```
 */
open class AcpKmpExtension {
    /** Ktor Client（core/logging/content-negotiation/json + 各平台 engine） */
    var network: Boolean = true

    /** Koin（core/compose/compose-viewmodel） */
    var di: Boolean = true

    /** Coil 3 图片加载（compose + network-ktor3） */
    var images: Boolean = true

    /** multiplatform-settings KV 存储 */
    var storage: Boolean = true

    /** Jetbrains navigation-compose（KMP 版） */
    var navigation: Boolean = true

    /** Jetbrains lifecycle-viewmodel / runtime-compose */
    var lifecycle: Boolean = true

    /** kotlinx-datetime 日期时间 */
    var datetime: Boolean = true
}

/**
 * KMP 共享模块约定插件（id: `acp.kmp.library`）。
 *
 * ## 契约（改这个插件前先读）
 * 它负责三件事，其余一概不管：
 * 1. 应用基础插件：kotlin-multiplatform + AGP KMP 库插件 + 序列化 + Compose + ktlint；
 * 2. 注册四个目标平台：**android / iosArm64+iosSimulatorArm64 / desktop(jvm) / wasmJs**；
 * 3. 按 [AcpKmpExtension] 开关，向各源集注入「平台对齐」的依赖。
 *
 * ## 调用方（模块）负责
 * - `kotlin { android { namespace / compileSdk / minSdk } }`：namespace 各模块必须显式写，
 *   避免模板复制后互相撞包名；
 * - iOS Framework 的 baseName（`shared` 模块里配了 `SharedKit`）；
 * - 只属于该模块的业务依赖。
 *
 * ## 为什么用 afterEvaluate
 * 模块脚本里的 `acp { }` 块在 plugin apply **之后**才执行，因此读取开关必须推迟到
 * afterEvaluate —— 这是「约定插件 + 可配置扩展」的标准做法（config cache 兼容）。
 *
 * ## 为什么不用 com.android.library
 * AGP 9.0 起 `com.android.library` 与 `org.jetbrains.kotlin.multiplatform` 互斥，
 * 必须用 `com.android.kotlin.multiplatform.library`，Android 配置也随之内迁到
 * `kotlin { android { } }` 块（见 `shared/build.gradle.kts`）。
 */
class AcpKmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(libs.findPlugin("kotlinMultiplatform").get().get().pluginId)
            apply(libs.findPlugin("androidMultiplatformLibrary").get().get().pluginId)
            apply(libs.findPlugin("kotlinSerialization").get().get().pluginId)
            apply(libs.findPlugin("composeMultiplatform").get().get().pluginId)
            apply(libs.findPlugin("composeCompiler").get().get().pluginId)
        }

        configureKtlint()
        extensions.create<AcpKmpExtension>("acp")

// ========== 新增：锁定 Compose 核心库版本，消除版本不匹配警告 ==========
        // 放在项目根 build.gradle.kts 顶层
//        with(target) {
//            configurations.all {
//                resolutionStrategy.eachDependency { dep ->
//                    val composeVersionProvider = libs.findVersion("compose")
//                    composeVersionProvider.ifPresent { ver ->
//                        if (dep.requested.group in setOf(
//                                "androidx.compose.ui",
//                                "androidx.compose.foundation",
//                                "androidx.compose.runtime"
//                            )
//                        ) {
//                            dep.useVersion(ver)
//                        }
//                    }
//                }
//            }
//        }





// =====================================================================


        // =====================================================================

        val composeDeps = extensions.getByType<ComposeExtension>().dependencies

        extensions.configure<KotlinMultiplatformExtension> {
            // ── 目标平台 ──────────────────────────────────────────────
            // Android target 由 AGP 的 KMP 库插件自动创建，这里不写 androidTarget()。
            jvm("desktop")

            iosArm64()
            iosSimulatorArm64()

            @OptIn(ExperimentalWasmDsl::class)
            wasmJs { browser() }

            // 默认层级模板：自动建立 commonMain / androidMain / iosMain / desktopMain / wasmJsMain
            applyDefaultHierarchyTemplate()

            sourceSets.apply {
                // ── 基线依赖：几乎必用，不受开关控制 ──────────────────
                commonMain.dependencies {
                    implementation(libs.findBundle("compose").get())
                    implementation(libs.findLibrary("kotlinx-coroutines-core").get())
                    implementation(libs.findLibrary("kotlinx-serialization-json").get())
                }

                commonTest.dependencies {
                    implementation(libs.findLibrary("kotlin-test").get())
                    implementation(libs.findLibrary("kotlinx-coroutines-test").get())
                    implementation(libs.findLibrary("ktor-client-mock").get())
                }

                // ── 平台专属基线依赖 ─────────────────────────────────
                androidMain.dependencies {
                    implementation(libs.findLibrary("androidx-core-ktx").get())
                }

                getByName("desktopMain").dependencies {
                    // Compose for Desktop 需要宿主 OS 的 skiko 原生库
                    implementation(composeDeps.desktop.currentOs)
                    implementation(libs.findLibrary("kotlinx-coroutines-swing").get())
                }
            }
        }

        // ── 可开关依赖 ───────────────────────────────────────────────
        // 必须等模块脚本的 acp { } 执行完（afterEvaluate），所以放在 Project 作用域里。
        afterEvaluate {
            val ext = extensions.getByType<AcpKmpExtension>()

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.apply {
                    commonMain.dependencies {
                        if (ext.datetime) implementation(libs.findLibrary("kotlinx-datetime").get())
                        if (ext.lifecycle) {
                            implementation(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                            implementation(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                        }
                        if (ext.navigation) implementation(libs.findLibrary("compose-navigation").get())
                        if (ext.network) implementation(libs.findBundle("ktor-client").get())
                        if (ext.di) {
                            implementation(libs.findLibrary("koin-core").get())
                            implementation(libs.findLibrary("koin-compose").get())
                            implementation(libs.findLibrary("koin-compose-viewmodel").get())
                        }
                        if (ext.images) {
                            implementation(libs.findLibrary("coil-compose").get())
                            implementation(libs.findLibrary("coil-network-ktor3").get())
                        }
                        if (ext.storage) {
                            implementation(libs.findLibrary("multiplatform-settings").get())
                            implementation(libs.findLibrary("multiplatform-settings-coroutines").get())
                        }
                    }

                    // Ktor 引擎是「平台对齐」的，必须分派到各自源集：
                    // OkHttp(Android/Desktop) / Darwin(iOS) / JS(Web)
                    if (ext.network) {
                        androidMain.dependencies { implementation(libs.findLibrary("ktor-client-okhttp").get()) }
                        getByName("desktopMain").dependencies { implementation(libs.findLibrary("ktor-client-okhttp").get()) }
                        getByName("iosMain").dependencies { implementation(libs.findLibrary("ktor-client-darwin").get()) }
                        getByName("wasmJsMain").dependencies { implementation(libs.findLibrary("ktor-client-js").get()) }
                    }
                }
            }
        }
    }
}
