package com.pregnancydiet.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pregnancydiet.app.ui.components.PlaceholderScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancyDietNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Pregnancy Diet Tracker") })
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppRoute.Home.route) {
                PlaceholderScreen(
                    title = "Welcome",
                    description = "Your pregnancy diet, symptom, supplement, and nutrition tracker foundation is ready.",
                )
            }
            composable(AppRoute.Auth.route) {
                PlaceholderScreen(
                    title = AppRoute.Auth.title,
                    description = "Firebase Google sign-in will be added in the authentication phase.",
                )
            }
            composable(AppRoute.Onboarding.route) {
                PlaceholderScreen(
                    title = AppRoute.Onboarding.title,
                    description = "Pregnancy dating, weight, allergies, and restrictions will be collected here.",
                )
            }
            composable(AppRoute.Pregnancy.route) {
                PlaceholderScreen(
                    title = AppRoute.Pregnancy.title,
                    description = "Pregnancy progress and due-date tracking will appear here.",
                )
            }
            composable(AppRoute.Symptoms.route) {
                PlaceholderScreen(
                    title = AppRoute.Symptoms.title,
                    description = "Symptom logging and red-flag safety checks will be implemented in a later phase.",
                )
            }
            composable(AppRoute.Supplements.route) {
                PlaceholderScreen(
                    title = AppRoute.Supplements.title,
                    description = "Prescribed supplement and pill tracking will be implemented in a later phase.",
                )
            }
            composable(AppRoute.Meals.route) {
                PlaceholderScreen(
                    title = AppRoute.Meals.title,
                    description = "Meal logging by food, quantity, unit, and grams will be implemented in a later phase.",
                )
            }
            composable(AppRoute.Nutrition.route) {
                PlaceholderScreen(
                    title = AppRoute.Nutrition.title,
                    description = "Pregnancy-stage-aware nutrition calculations will be implemented in a later phase.",
                )
            }
            composable(AppRoute.AiSummary.route) {
                PlaceholderScreen(
                    title = AppRoute.AiSummary.title,
                    description = "Educational AI summaries will use a backend proxy in a later phase.",
                )
            }
        }
    }
}