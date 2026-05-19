package com.example.nexusbank.core.security.screenshot

import android.view.Window
import android.view.WindowManager

/**
 * Apply [WindowManager.LayoutParams.FLAG_SECURE] to a window to block
 * screenshots, screen recording, and obscure the app preview in the
 * recent-apps switcher. Use on sensitive screens (transfer confirm,
 * statement details, MPIN entry).
 *
 * For Compose, prefer the `PreventScreenshots` composable in
 * `com.example.nexusbank.core.ui.screenshot` which scopes the flag to
 * the composition lifetime.
 */
fun Window.preventScreenshots() {
    addFlags(WindowManager.LayoutParams.FLAG_SECURE)
}

/** Counterpart — re-enable screenshots when leaving the sensitive screen. */
fun Window.allowScreenshots() {
    clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
}

