package com.acp923.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.tooling.preview.Preview
import com.acp923.shared.ui.App

/**
 * Android 壳入口 —— 这里**只做平台接线，不写业务 UI**。
 *
 * 职责：Activity 生命周期、窗口样式（edge-to-edge）、以及把共享 UI 挂到 Compose 树上。
 * 新增 Android 能力（权限、通知、深链）时，优先在 shared/androidMain 里封装成
 * expect/actual 能力，而不是把业务逻辑搬到这里。
 *
 * 需要依赖注入（Koin）时，在本类的 `onCreate` 前调用 `initKoin()`，
 * 但注意 Application/Activity 是 Android 独有 —— 组装逻辑放 shared 的 `startKoin` 里更通用。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}
