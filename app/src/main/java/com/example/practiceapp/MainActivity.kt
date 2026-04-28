package com.example.practiceapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.nexusbank.core.ui.theme.NexusBankTheme
import com.example.practiceapp.navigation.NexusBankNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexusBankTheme(darkTheme = false, dynamicColor = false) {
                val navController = rememberNavController()
                NexusBankNavHost(navController = navController)
            }
        }
    }
}