package com.pregnancydiet.app.auth

import com.pregnancydiet.app.model.AuthenticatedUser
import com.pregnancydiet.app.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthRouteResolverTest {
    @Test
    fun `signed out users route to login`() {
        assertEquals(AuthDestination.SignedOut, AuthRouteResolver.resolve(user = null, profile = null))
    }

    @Test
    fun `signed in users without onboarding route to onboarding`() {
        val user = testUser()
        val profile = testProfile(onboardingCompleted = false, activePregnancyProfileId = null)

        assertEquals(AuthDestination.NeedsOnboarding, AuthRouteResolver.resolve(user, profile))
    }

    @Test
    fun `signed in users with an active pregnancy profile route to home`() {
        val user = testUser()
        val profile = testProfile(onboardingCompleted = true, activePregnancyProfileId = "profile_123")

        assertEquals(AuthDestination.Home, AuthRouteResolver.resolve(user, profile))
    }

    private fun testUser() = AuthenticatedUser(
        uid = "uid_123",
        email = "user@example.com",
        displayName = "User",
        photoUrl = null,
    )

    private fun testProfile(
        onboardingCompleted: Boolean,
        activePregnancyProfileId: String?,
    ) = UserProfile(
        uid = "uid_123",
        email = "user@example.com",
        displayName = "User",
        photoUrl = null,
        onboardingCompleted = onboardingCompleted,
        activePregnancyProfileId = activePregnancyProfileId,
    )
}