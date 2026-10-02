import SwiftUI
import SharedKit

/// iOS 应用入口。
///
/// `SharedKit` 就是 shared 模块里 `binaries.framework { baseName = "SharedKit" }` 的产物。
/// Kotlin 顶层函数 `MainViewController()` 在 Swift 侧被命名为
/// `MainViewControllerKt.MainViewController()`（文件名 + Kt 前缀）。
@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea(.keyboard)
        }
    }
}
