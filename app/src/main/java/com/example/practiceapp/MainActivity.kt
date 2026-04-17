package com.example.practiceapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.practiceapp.ui.screens.AIChatScreen
import com.example.practiceapp.ui.theme.PracticeAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PracticeAppTheme(darkTheme = true, dynamicColor = false) {
                AIChatScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}