package com.acp923.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.acp923.shared.ui.App

/**
 * Desktop (JVM) 入口 —— 只做窗口接线。
 *
 * - `mainClass` 在 build.gradle.kts 里指向 `com.acp923.desktop.MainKt`，
 *   改包名/文件名时两处要同步改。
 * - 打包安装包用 `gradlew.bat :desktopApp:packageDistributionForCurrentOS`（需配 jpackage，JDK 14+）。
 * - 想要热重载：装 JBR 21 后打开 gradle.properties 里的 `compose.reload.jbr.binary`。
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ACP-923",
        state = rememberWindowState(width = 480.dp, height = 360.dp),
    ) {
        App()
    }
}
