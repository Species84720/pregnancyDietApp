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
import com.pregnancydiet.app.ui.screens.AiSummaryScreen
import com.pregnancydiet.app.ui.screens.AuthenticatedPlaceholderScreen
import com.pregnancydiet.app.ui.screens.HomeDashboardScreen
import com.pregnancydiet.app.ui.screens.LoadingScreen
import com.pregnancydiet.app.ui.screens.LoginScreen
import com.pregnancydiet.app.ui.screens.MealLoggingScreen
import com.pregnancydiet.app.ui.screens.NutritionSummaryScreen
import com.pregnancydiet.app.ui.screens.OnboardingScreen
import com.pregnancydiet.app.ui.screens.SupplementTrackingScreen
import com.pregnancydiet.app.ui.screens.SymptomLoggingScreen

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
                HomeDashboardScreen(
                    uid = authState.user?.uid,
                    onAddMeal = { navController.navigate(AppRoute.Meals.route) },
                    onAddSymptom = { navController.navigate(AppRoute.Symptoms.route) },
                    onAddSupplement = { navController.navigate(AppRoute.Supplements.route) },
                    onViewNutrition = { navController.navigate(AppRoute.Nutrition.route) },
                    onViewAiSummary = { navController.navigate(AppRoute.AiSummary.route) },
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
                SymptomLoggingScreen(
                    uid = authState.user?.uid,
                    onBackToHome = { navController.navigate(AppRoute.Home.route) },
                )
            }
            composable(AppRoute.Supplements.route) {
                SupplementTrackingScreen(
                    uid = authState.user?.uid,
                    onBackToHome = { navController.navigate(AppRoute.Home.route) },
                )
            }
            composable(AppRoute.Meals.route) {
                MealLoggingScreen(
                    uid = authState.user?.uid,
                    onBackToHome = { navController.navigate(AppRoute.Home.route) },
                )
            }
            composable(AppRoute.Nutrition.route) {
                NutritionSummaryScreen(
                    uid = authState.user?.uid,
                    onBackToHome = { navController.navigate(AppRoute.Home.route) },
                )
            }
            composable(AppRoute.AiSummary.route) {
                AiSummaryScreen(
                    uid = authState.user?.uid,
                    onBackToHome = { navController.navigate(AppRoute.Home.route) },
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