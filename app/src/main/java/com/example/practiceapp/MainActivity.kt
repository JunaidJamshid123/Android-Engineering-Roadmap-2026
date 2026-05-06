package com.example.practiceapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.nexusbank.core.security.EncryptedPrefs
import com.example.nexusbank.core.ui.theme.NexusBankTheme
import com.example.practiceapp.navigation.NexusBankNavHost
import com.example.practiceapp.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var encryptedPrefs: EncryptedPrefs

    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexusBankTheme(darkTheme = false, dynamicColor = false) {
                val nav = rememberNavController()
                navController = nav
                NexusBankNavHost(navController = nav)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (encryptedPrefs.isLoggedIn() && encryptedPrefs.isSessionExpired()) {
            encryptedPrefs.clearSession()
            navController?.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (encryptedPrefs.isLoggedIn()) {
            encryptedPrefs.updateLastActive()
        }
    }
}