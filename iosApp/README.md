# iosApp（iOS 壳工程）

这里是 iOS 侧的**壳**：SwiftUI 应用 + 桥接 Kotlin 侧 `SharedKit` Framework。
界面本身全部来自 `shared` 模块（`MainViewControllerKt.MainViewController()`）。

## 为什么没有 .xcodeproj

`.xcodeproj` 是二进制/易冲突文件，且**只能在 macOS 上生成与维护**。
模板只保留可读的 Swift 源码与说明，避免在 Windows 侧产生无法验证的工程文件。

## 在 macOS 上跑起来（一次性配置）

1. Xcode → File → New → Project → iOS App，名称 `ACP-923`，语言 Swift，界面 SwiftUI，
   存放在 `iosApp/` 目录下（生成 `iosApp.xcodeproj`）。
   或者直接新建工程后把本目录的 `iOSApp.swift` / `ContentView.swift` 拖入工程。
2. 构建 Kotlin Framework（在项目根目录）：
   ```bash
   ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
   ```
3. Xcode 工程 → Target → Build Settings：
   - `FRAMEWORK_SEARCH_PATHS` 追加
     `$(SRCROOT)/../shared/build/bin/iosSimulatorArm64/debugFramework`
   - `OTHER_LDFLAGS` 追加 `-framework SharedKit`
   - 真机换成 `iosArm64/debugFramework` 路径。
4. 把 Framework 拖进 Target → General → Frameworks, Libraries（Embed = Do Not Embed，因为是静态库）。
5. 运行：`./gradlew :shared:embedAndSignAppleFrameworkForXcode` 也支持从 Xcode 的
   Build Phase 直接触发（Run Script Phase 里调用即可实现「Xcode 构建时自动编 Kotlin」）。

## 目录

```
iosApp/
├── iosApp/
│   ├── iOSApp.swift       # @main 入口
│   ├── ContentView.swift  # UIViewControllerRepresentable 桥接
│   └── Info.plist
└── README.md
```
