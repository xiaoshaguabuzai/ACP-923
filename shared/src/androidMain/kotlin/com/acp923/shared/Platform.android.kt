package com.acp923.shared

/** Android 平台标识。真正的平台能力（Context、权限、通知等）从这里向 common 暴露。 */
actual val currentPlatform: Platform = Platform.ANDROID
