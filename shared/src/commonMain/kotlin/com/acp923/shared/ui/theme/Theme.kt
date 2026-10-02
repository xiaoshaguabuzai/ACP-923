package com.acp923.shared.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * 应用主题**唯一入口**。
 *
 * ## 架构约定
 * - 所有颜色 / 字体 / 形状都收在这里，通过 `MaterialTheme.colorScheme` 等消费；
 *   UI 代码里**不要**出现裸 `Color(0xFF...)`，否则换肤、深色模式、品牌定制会失控。
 * - 多模块项目里，这个文件通常会长成独立模块 `core:designsystem`（见 docs/ARCHITECTURE.md），
 *   到时把本文件整体搬过去即可，包名保持稳定。
 * - 平台差异（如桌面侧的系统暗色开关）通过参数注入，不要把 `isSystemInDarkTheme()`
 *   散落到各个 Composable 里。
 */
private val LightColors = lightColorScheme()

private val DarkColors = darkColorScheme()

@Composable
fun AcpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
