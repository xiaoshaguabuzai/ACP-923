package com.acp923.shared.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acp923.shared.currentPlatform
import com.acp923.shared.displayName
import com.acp923.shared.generated.resources.Res
import com.acp923.shared.generated.resources.app_name
import com.acp923.shared.generated.resources.counter_increment
import com.acp923.shared.generated.resources.counter_reset
import com.acp923.shared.generated.resources.counter_value
import com.acp923.shared.generated.resources.platform_label
import com.acp923.shared.presentation.CounterState
import com.acp923.shared.presentation.CounterViewModel
import com.acp923.shared.ui.theme.AcpTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 应用根 Composable —— **所有平台入口唯一的注入点**。
 *
 * ```
 * Android : MainActivity  → setContent { App() }
 * Desktop : Main.kt       → Window { App() }
 * Web     : Main.kt       → ComposeViewport { App() }
 * iOS     : MainViewController() → ComposeUIViewController { App() }
 * ```
 *
 * ## 用到的范式
 * | 关注点 | 做法 |
 * |---|---|
 * | 主题 | [AcpTheme] 包一层，UI 内只消费 `MaterialTheme.*` |
 * | 状态 | `ViewModel` + `StateFlow`，UI 侧 `collectAsStateWithLifecycle()` |
 * | 文案 | Compose Resources（`Res.string.*`），不写硬编码字符串 |
 * | 平台差异 | `currentPlatform`（expect/actual） |
 *
 * ## 新项目接入顺序建议
 * 1. 先替换本文件的 Scaffold 内容为自己的首页；
 * 2. 需要导航时引入 `navigation-compose`（已在依赖里，`acp { navigation = true }`）；
 * 3. 需要 DI 时在 App 外层 `KoinApplication { }`（见 docs/ARCHITECTURE.md 的 Koin 接法）。
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    AcpTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text(stringResource(Res.string.app_name)) }) },
        ) { padding ->
            HomeScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
            )
        }
    }
}

/**
 * 首页。注意这个 Composable **自己不持有状态**：
 * 状态全部来自 [CounterViewModel]，方便预览、测试和后续换实现。
 */
@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: CounterViewModel = viewModel { CounterViewModel() },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.platform_label, currentPlatform.displayName),
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(Res.string.counter_value, state.count),
            style = MaterialTheme.typography.headlineSmall,
        )

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = viewModel::increment) {
                Text(stringResource(Res.string.counter_increment))
            }
            OutlinedButton(onClick = viewModel::reset) {
                Text(stringResource(Res.string.counter_reset))
            }
        }
    }
}


