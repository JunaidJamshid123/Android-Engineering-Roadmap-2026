package com.example.practiceapp

import android.app.Application
import android.os.StrictMode
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NexusBankApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }
    }

    /**
     * Surfaces accidental disk / network I/O on the main thread, leaked
     * Closeable / Cursor / Activity instances, and other common policy
     * violations during development. Disabled in release builds.
     */
    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .detectCustomSlowCalls()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectLeakedRegistrationObjects()
                .detectActivityLeaks()
                .penaltyLog()
                .build()
        )
    }
}
