import SwiftUI
import SharedKit

/// 把 Kotlin 侧的 Compose 界面包装成 SwiftUI 视图。
///
/// `MainViewControllerKt.MainViewController()` 返回 `UIViewController`，
/// 由 `UIViewControllerRepresentable` 桥接给 SwiftUI —— 这是 CMP 官方推荐的接入方式。
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
    }
}
