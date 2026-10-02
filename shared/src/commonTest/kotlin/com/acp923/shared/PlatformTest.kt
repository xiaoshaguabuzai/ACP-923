package com.acp923.shared

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 测试示例：commonTest 里的用例会在**每个平台**上各跑一遍
 * （desktopTest / androidUnitTest / iosSimulatorArm64Test / wasmJsTest）。
 * 需要按平台断言的用例，放到对应源集的测试目录里。
 */
class PlatformTest {
    @Test
    fun platformIsReported() {
        assertTrue(currentPlatform.displayName.isNotBlank())
    }
}
