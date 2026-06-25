package com.pregnancydiet.app.ui

import androidx.compose.runtime.Composable
import com.pregnancydiet.app.ui.navigation.PregnancyDietNavHost
import com.pregnancydiet.app.ui.theme.PregnancyDietTheme

@Composable
fun PregnancyDietApp() {
    PregnancyDietTheme {
        PregnancyDietNavHost()
    }
}