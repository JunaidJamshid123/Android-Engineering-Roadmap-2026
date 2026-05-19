package com.example.nexusbank.core.ui.screenshot

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

/**
 * Apply [WindowManager.LayoutParams.FLAG_SECURE] to the host window for
 * the lifetime of the composition. Use on sensitive screens (transfer
 * confirm, statement details, MPIN entry) to block screenshots, screen
 * recording, and obscure the app preview in the recent-apps switcher.
 *
 * Re-enables screenshots when the composable leaves the composition so
 * non-sensitive screens behave normally.
 *
 * Usage:
 * ```
 * @Composable
 * fun TransferConfirmScreen(...) {
 *     PreventScreenshots()
 *     // ...screen content
 * }
 * ```
 */
@Composable
fun PreventScreenshots() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
