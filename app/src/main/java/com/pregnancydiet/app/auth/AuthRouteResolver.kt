package com.pregnancydiet.app.auth

import com.pregnancydiet.app.model.AuthenticatedUser
import com.pregnancydiet.app.model.UserProfile

object AuthRouteResolver {
    fun resolve(
        user: AuthenticatedUser?,
        profile: UserProfile?,
    ): AuthDestination = when {
        user == null -> AuthDestination.SignedOut
        profile?.onboardingCompleted == true && !profile.activePregnancyProfileId.isNullOrBlank() -> AuthDestination.Home
        else -> AuthDestination.NeedsOnboarding
    }
}