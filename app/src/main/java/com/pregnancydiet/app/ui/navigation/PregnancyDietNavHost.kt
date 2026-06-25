package com.pregnancydiet.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pregnancydiet.app.auth.AuthDestination
import com.pregnancydiet.app.auth.AuthViewModel
import com.pregnancydiet.app.ui.screens.AuthenticatedPlaceholderScreen
import com.pregnancydiet.app.ui.screens.LoadingScreen
import com.pregnancydiet.app.ui.screens.LoginScreen
import com.pregnancydiet.app.ui.screens.OnboardingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancyDietNavHost(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(authState.destination) {
        val targetRoute = authState.destination.toRoute()
        if (navController.currentDestination?.route != targetRoute) {
            navController.navigate(targetRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Pregnancy Diet Tracker") },
                actions = {
                    if (authState.user != null) {
                        TextButton(onClick = authViewModel::signOut) {
                            Text("Sign out")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Splash.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppRoute.Splash.route) {
                LoadingScreen()
            }
            composable(AppRoute.Home.route) {
                AuthenticatedPlaceholderScreen(
                    title = "Home",
                    description = "Your pregnancy dashboard will appear here after onboarding is complete.",
                    user = authState.user,
                    errorMessage = authState.errorMessage,
                )
            }
            composable(AppRoute.Auth.route) {
                LoginScreen(
                    isLoading = authState.isLoading,
                    errorMessage = authState.errorMessage,
                    onGoogleIdToken = authViewModel::signInWithGoogle,
                    onError = authViewModel::showError,
                )
            }
            composable(AppRoute.Onboarding.route) {
                OnboardingScreen(
                    user = authState.user,
                    onCompleted = authViewModel::refreshUserProfile,
                )
            }
            composable(AppRoute.Pregnancy.route) {
                AuthenticatedPlaceholderScreen(
                    title = AppRoute.Pregnancy.title,
                    description = "Pregnancy progress and due-date tracking will appear here.",
                    user = authState.user,
                    errorMessage = authState.errorMessage,
                )
            }
            composable(AppRoute.Symptoms.route) {
                AuthenticatedPlaceholderScreen(
                    title = AppRoute.Symptoms.title,
                    description = "Symptom logging and red-flag safety checks will be implemented in a later phase.",
                    user = authState.user,
                    errorMessage = authState.errorMessage,
                )
            }
            composable(AppRoute.Supplements.route) {
                AuthenticatedPlaceholderScreen(
                    title = AppRoute.Supplements.title,
                    description = "Prescribed supplement and pill tracking will be implemented in a later phase.",
                    user = authState.user,
                    errorMessage = authState.errorMessage,
                )
            }
            composable(AppRoute.Meals.route) {
                AuthenticatedPlaceholderScreen(
                    title = AppRoute.Meals.title,
                    description = "Meal logging by food, quantity, unit, and grams will be implemented in a later phase.",
                    user = authState.user,
                    errorMessage = authState.errorMessage,
                )
            }
            composable(AppRoute.Nutrition.route) {
                AuthenticatedPlaceholderScreen(
                    title = AppRoute.Nutrition.title,
                    description = "Pregnancy-stage-aware nutrition calculations will be implemented in a later phase.",
                    user = authState.user,
                    errorMessage = authState.errorMessage,
                )
            }
            composable(AppRoute.AiSummary.route) {
                AuthenticatedPlaceholderScreen(
                    title = AppRoute.AiSummary.title,
                    description = "Educational AI summaries will use a backend proxy in a later phase.",
                    user = authState.user,
                    errorMessage = authState.errorMessage,
                )
            }
        }
    }
}

private fun AuthDestination.toRoute(): String = when (this) {
    AuthDestination.Loading -> AppRoute.Splash.route
    AuthDestination.SignedOut -> AppRoute.Auth.route
    AuthDestination.NeedsOnboarding -> AppRoute.Onboarding.route
    AuthDestination.Home -> AppRoute.Home.route
}