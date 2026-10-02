plugins {
    `kotlin-dsl`
}

group = "com.acp923.buildlogic"

dependencies {
    // 用 implementation（而非 compileOnly），保证约定插件运行时能拿到这些插件。
    // 这里每加一行，build-logic 的编译期就会解析对应插件 —— 保持精简。
    implementation(libs.plugins.androidApplication.toDep())
    implementation(libs.plugins.androidMultiplatformLibrary.toDep())
    implementation(libs.plugins.kotlinMultiplatform.toDep())
    implementation(libs.plugins.kotlinSerialization.toDep())
    implementation(libs.plugins.composeMultiplatform.toDep())
    implementation(libs.plugins.composeCompiler.toDep())
    implementation(libs.plugins.ktlint.toDep())
}

/**
 * 把版本目录里的插件声明转成 build-logic 能直接依赖的坐标：
 * `id` → `id:id.gradle.plugin:version`
 */
fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}

gradlePlugin {
    plugins {
        register("acpKmpLibrary") {
            id = "acp.kmp.library"
            implementationClass = "com.acp923.buildlogic.AcpKmpLibraryConventionPlugin"
        }
        register("acpAndroidApp") {
            id = "acp.android.app"
            implementationClass = "com.acp923.buildlogic.AcpAndroidAppConventionPlugin"
        }
    }
}
