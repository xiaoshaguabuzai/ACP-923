// ACP-923 项目模板 —— 新项目从这里复制改起点即可。
//
// ═══════════════════════════════════════════════════════════════
// 仓库顺序说明（本机踩坑总结，勿随意改动）
//
// 【依赖】官方仓库在前、镜像兜底：
//   Gradle 对「某个模块的产物」只会去「提供该模块元数据的那个仓库」里取。
//   若把镜像放最前面，KMP 的冷门产物（*-metadata.jar）在镜像冷启动时
//   常被 404，且 Gradle 会把「找不到」永久缓存 —— iOS/native 的
//   *ForIde 任务会集体失败。中了就跑：gradlew.bat --refresh-dependencies
//
// 【插件】镜像在 gradlePluginPortal() 之前：
//   本机访问 plugins.gradle.org 会被 reset（Connection reset），
//   而 Gradle 在某仓库遇到 I/O 异常会中止整条仓库链 —— portal 排前面
//   会导致 kotlin-dsl / ktor 等插件「明明镜像里有却报 not found」。
// ═══════════════════════════════════════════════════════════════

rootProject.name = "ACP-923"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        // 插件镜像优先，解决 Connection reset
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/public")

        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()

        // 本机大概率不可达，放最后兜底
        gradlePluginPortal()
    }
}


dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()

        // 依赖兜底镜像（官方拿不到时才命中）
        maven("https://maven.aliyun.com/repository/public")
    }
}
plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"}

// ── 模块注册 ─────────────────────────────────────────────────────────────
// 命名约定：壳模块用 `xxxApp`，共享/库模块用扁平名（shared、core-xxx、feature-xxx）。
// 新增 KMP 共享模块：目录建好后在这里 include，模块内用 acp.kmp.library 约定插件，
// 并显式写 namespace —— 别依赖路径推导，避免复制模块时撞包名。
// 拆分时机与依赖方向规则见 docs/ARCHITECTURE.md 第 7 节。
include(":shared")
include(":androidApp")
include(":desktopApp")
include(":webApp")
// 注意：iosApp 不在这里 include —— 它由 Xcode 工程管理，只通过 SharedKit Framework 与 Kotlin 交互。
