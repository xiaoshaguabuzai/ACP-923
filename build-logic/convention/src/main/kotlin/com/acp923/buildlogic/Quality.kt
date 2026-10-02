package com.acp923.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

/**
 * 代码风格门禁：ktlint（版本在 `gradle/libs.versions.toml` 的 `ktlint` 里锁定）。
 *
 * 规则配置以根目录 `.editorconfig` 为唯一来源 —— 不要在模块里另起一套，
 * 否则 IDE 格式化与 CI 校验会互相打架。
 *
 * 常用命令：
 * - `gradlew.bat ktlintCheck` 全量校验
 * - `gradlew.bat ktlintFormat` 自动修复（import 排序、缩进、尾逗号等）
 */
internal fun Project.configureKtlint() {
    pluginManager.apply("org.jlleitschuh.gradle.ktlint")

    extensions.configure<KtlintExtension> {
        // Android 源集（src/main/kotlin）也要纳入扫描
        android.set(true)
        // 校验失败即构建失败，避免风格债悄悄累积
        ignoreFailures.set(false)
    }
}
