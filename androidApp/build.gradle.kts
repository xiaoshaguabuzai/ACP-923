// Android 应用模块 —— 壳，只做平台接线。
//
// 通用配置（compileSdk/minSdk/targetSdk、JVM 17、Compose 插件、ktlint）来自
// build-logic 的 `acp.android.app` 约定插件；这里只声明"本 App 独有"的东西。
plugins {
    alias(libs.plugins.acpAndroidApp)
}

android {
    // namespace 与 applicationId 分开声明：前者是代码包名（R 类所在），
    // 后者是安装标识。改包名只动这两处 + 源码目录。
    namespace = "com.acp923.android"

    defaultConfig {
        applicationId = "com.acp923.android"
        // 版本号来自版本目录，方便多 App 统一发版（也可直接写死）
        versionCode = libs.versions.android.versionCode.get().toInt()
        versionName = libs.versions.android.versionName.get()
    }

    buildTypes {
        release {
            // 混淆规则见 proguard-rules.pro（Ktor/Koin/序列化引入后需要补 keep 规则）
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    // 预览/检查工具只在 debug 变体里带，别进 release
    debugImplementation(libs.compose.ui.tooling)
}
