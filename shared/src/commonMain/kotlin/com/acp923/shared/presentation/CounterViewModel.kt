package com.acp923.shared.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 界面状态：**不可变** data class。
 * 约定：状态只增字段、只在 ViewModel 内构造；UI 只读不写。
 */
data class CounterState(val count: Int = 0)

/**
 * ViewModel 范式（KMP 全平台通用）。
 *
 * ## 为什么用 ViewModel 而不是 `remember { }`
 * - 状态随配置变化（Android 旋转、桌面窗口重建）存活；
 * - 与 UI 解耦，可被 commonTest 直接单测；
 * - 换成 Koin 注入依赖时不需要改 UI 层签名（构造参数从 `CounterViewModel()` 变成
 *   `CounterViewModel(repository)`，装配在 DI 里做）。
 *
 * ## 约定
 * - 一个 ViewModel 只暴露一个 [StateFlow] 给 UI，避免 UI 层做状态拼接；
 * - 副作用（网络、DB）走 `viewModelScope`，别用全局 scope；
 * - 需要跨模块复用/被 iOS 侧调用时，把 ViewModel 放在 `presentation` 包下并在
 *   iosMain 里导出（Kotlin 类默认可见，Swift 侧通过 Framework 头文件调用）。
 */
class CounterViewModel : ViewModel() {

    private val _state = MutableStateFlow(CounterState())
    val state: StateFlow<CounterState> = _state.asStateFlow()

    fun increment() {
        _state.update { it.copy(count = it.count + 1) }
    }

    fun reset() {
        _state.update { CounterState() }
    }
}
