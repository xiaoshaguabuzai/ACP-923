// build-logic 是一个独立的 included build。
//
// ⚠️ 两个必须记住的事实（这是本机最容易踩的坑）：
// 1. included build **不继承**根项目的仓库配置（settings 与 gradle.properties 都不共享），
//    所以下面这些仓库、以及 26 行往下的版本目录指向，必须在这里再写一遍。
// 2. 插件解析顺序：本机访问 plugins.gradle.org 会被 reset（SocketException: Connection reset），
//    而 Gradle 一旦在某个仓库遇到 I/O 异常就会中止整条仓库链 —— 排在后面的仓库根本不会被请求。
//    因此阿里云镜像必须排在 gradlePluginPortal() **之前**，否则 `kotlin-dsl`
//    （只发布在 Plugin Portal 上）永远解析不到，报 Plugin [...] was not found。
//
// 依赖（dependencyResolutionManagement）的取向相反：官方优先、镜像兜底，见根 settings.gradle.kts 的说明。
pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/public")
    }

    // 指针式复用根项目的版本目录：约定插件里才能写 libs.findLibrary("ktor-client-core")
    // 而不用把版本号抄一遍。改版本只改根项目 gradle/libs.versions.toml。
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
