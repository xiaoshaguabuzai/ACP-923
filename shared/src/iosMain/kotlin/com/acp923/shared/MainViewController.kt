package com.acp923.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.acp923.shared.ui.App

/**
 * iOS 入口（由 Xcode 工程调用）。
 *
 * 注意 Kotlin 顶层函数在 Framework 里会被命名为 `MainViewControllerKt.MainViewController()`，
 * Swift 侧调用见 `iosApp/ContentView.swift`。
 *
 * 该文件位于 iosMain —— 也就是**只有 iOS 目标**参与编译，
 * 因此实际打包必须在 macOS 上执行（Windows 只能做编译期校验）。
 */
fun MainViewController() = ComposeUIViewController { App() }
