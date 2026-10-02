// Desktop (JVM) 应用模块 —— 壳，只做窗口接线。
//
// 这里用 kotlin-jvm 插件（不是 KMP）：Desktop 只是"又一个 JVM 目标"，
// 真正的多平台代码在 shared 里。
//
// ⚠️ java 兼容块必须与 Kotlin jvmTarget 一致（都是 17），
// 否则 Gradle 会报 Inconsistent JVM-target compatibility（Gradle 跑在 JDK 21 上时尤其容易踩）。
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":shared"))
    // Desktop 需要宿主 OS 的 skiko 原生库
    implementation(compose.desktop.currentOs)
    // 桌面 UI 在 Swing 事件线程上跑协程
    implementation(libs.kotlinx.coroutines.swing)
}

compose.desktop {
    application {
        // 与 desktopApp/src/main/kotlin/com/acp923/desktop/Main.kt 的包名+文件名对应
        mainClass = "com.acp923.desktop.MainKt"
    }
}
