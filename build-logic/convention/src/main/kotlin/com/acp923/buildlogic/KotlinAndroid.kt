package com.acp923.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * 所有 Android 模块（application / library）共用的基础配置。
 *
 * ## 版本来源
 * `compileSdk` / `minSdk` 一律从版本目录读（`libs.versions.android.compileSdk`），
 * 模块里不要再硬编码数字 —— 升级 SDK 只改一处。
 *
 * ## 命名空间
 * `namespace` 未显式声明时按模块路径推导（`:feature:home` → `com.acp923.feature.home`）。
 * 共享模块建议在模块里显式写 namespace，方便日后独立发布。
 *
 * ## 兼容性注记
 * AGP 9.x 的 `CommonExtension` 不再带泛型参数，`packaging` / `compileOptions` 等
 * 块要用 `.apply { }` 包一层再配置。
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension,
) {
    commonExtension.apply {
        compileSdk = libs.findVersion("android-compileSdk").get().toString().toInt()

        // namespace 未显式指定时，按模块路径自动推导：com.acp923.<模块路径>
        if (namespace == null) {
            namespace = "com.acp923" + path.replace(":", ".").replace("-", "")
        }

        defaultConfig.apply {
            minSdk = libs.findVersion("android-minSdk").get().toString().toInt()
        }

        // Android 侧的字节码目标固定 17：与 Kotlin jvmTarget、以及 kotlin-jvm 模块的
        // java 兼容块保持一致，否则会报 Inconsistent JVM-target compatibility。
        compileOptions.apply {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)

            // -Werror 开关：gradle.properties 里加 warningsAsErrors=true 即启用（CI 推荐开）
            val warningsAsErrors: String? by project
            allWarningsAsErrors.set(warningsAsErrors.toBoolean())

            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
            )
        }
    }
}
