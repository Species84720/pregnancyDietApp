package com.pregnancydiet.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pregnancydiet.app.ai.AiDependencyProvider
import com.pregnancydiet.app.ui.PregnancyDietApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AiDependencyProvider.initialize(applicationContext)
        enableEdgeToEdge()
        setContent {
            PregnancyDietApp()
        }
    }
}