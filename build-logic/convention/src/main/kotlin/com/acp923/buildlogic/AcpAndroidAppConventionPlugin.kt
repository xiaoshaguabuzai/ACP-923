package com.acp923.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Android 应用模块约定插件（id: `acp.android.app`）。
 *
 * ## 契约
 * 统一「所有 Android App 模块」的最小公共面：
 * - 插件：com.android.application + Compose Multiplatform + Compose 编译器 + ktlint；
 * - 由 `android-compileSdk` / `android-minSdk` / `android-targetSdk` 三个版本目录项统一下发 SDK 级别；
 * - Java/Kotlin 目标统一 JVM 17；打包排除 META-INF 常见冲突文件。
 *
 * ## 调用方（模块）必须自己声明
 * - `namespace`、`applicationId`、`versionCode`、`versionName`（各 App 独立演进，不应共用）；
 * - `buildTypes`、签名、`proguard` 规则等发布相关配置。
 *
 * ## 注意
 * - AGP 9 的 `CommonExtension` 已无泛型参数，配置函数签名见 [configureKotlinAndroid]；
 * - 若某模块需要 KMP（共享 UI 编译进 App），请改用 `acp.kmp.library` + 壳模块模式，
 *   不要把 kotlin-multiplatform 应用到 application 模块上。
 */
class AcpAndroidAppConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(libs.findPlugin("androidApplication").get().get().pluginId)
            apply(libs.findPlugin("composeMultiplatform").get().get().pluginId)
            apply(libs.findPlugin("composeCompiler").get().get().pluginId)
        }

        configureKtlint()

        extensions.configure<ApplicationExtension> {
            configureKotlinAndroid(this)

            defaultConfig {
                targetSdk = libs.findVersion("android-targetSdk").get().toString().toInt()
            }

            packaging {
                resources {
                    // Kotlin/Compose 依赖里重复的许可证文件会让打包失败，统一排除
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                }
            }
        }
    }
}
