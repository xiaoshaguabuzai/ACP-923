// 共享模块：业务逻辑 + Compose UI，四个平台共用。
//
// ┌─ 职责边界 ─────────────────────────────────────────────────────────┐
// │ 本文件只负责：Android 命名空间/SDK 级别、iOS Framework 打包方式、      │
// │ 以及 Compose Resources 的生成包名。                                │
// │ 目标平台注册、通用依赖注入全部由 build-logic 的 acp.kmp.library 负责。 │
// └───────────────────────────────────────────────────────────────────┘
//
// 依赖开关示例（不用的能力可以关掉，避免污染 classpath）：
//   acp {
//       images = false     // 不做图片加载 → 不引入 Coil
//       storage = false    // 不做本地 KV 存储
//   }
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.acpKmpLibrary)
}

kotlin {
    // AGP 9 的 KMP 库插件：Android 配置放在这里（不再有独立的 android {} 块）
    android {
        // namespace 必须显式写：共享模块可能被多个 App 复用，不能靠路径推导
        namespace = "com.acp923.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions.jvmTarget = JvmTarget.JVM_17
        // 打开后 Android 侧才会把 composeResources 的资源打进 AAR
        androidResources.enable = true
    }

    // iOS 目标已由约定插件创建，这里只补「打包成静态 Framework」的配置。
    // baseName 决定 Swift 侧 `import SharedKit` 的名字。
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "SharedKit"
            isStatic = true
        }
    }
}

// Compose Resources：把 commonMain/composeResources 下的资源生成 Res 类。
// 多模块时**每个模块都要指定不同的包名**，否则生成的 Res 会互相覆盖。
compose.resources {
    publicResClass = true
    packageOfResClass = "com.acp923.shared.generated.resources"
}
