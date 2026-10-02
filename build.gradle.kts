// 根构建脚本：**只声明插件版本，绝不在这里配置具体模块**。
//
// 为什么：模块的通用配置都在 build-logic 里以约定插件形式实现（单一事实来源），
// 根脚本一旦开始写 allprojects/subprojects 配置，模块之间的差异就会藏进"全局隐式行为"，
// 后面很难排查。需要新公共能力时，加约定插件，不要加 allprojects 块。
//
// `apply false` 的作用：把插件坐标交给这里解析（版本来自 libs.versions.toml），
// 但不在根项目上应用 —— 真正 apply 的是各模块或约定插件。
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.ktlint) apply false
}
