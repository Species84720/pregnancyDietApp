package com.pregnancydiet.app.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppRouteTest {
    @Test
    fun `home route is the default route`() {
        assertEquals("home", AppRoute.Home.route)
    }

    @Test
    fun `splash route is available for auth state loading`() {
        assertEquals("splash", AppRoute.Splash.route)
    }

    @Test
    fun `top level routes include primary tracking areas`() {
        val routes = AppRoute.topLevelRoutes.map { it.route }

        assertTrue(routes.containsAll(listOf("home", "pregnancy", "symptoms", "supplements", "meals", "nutrition")))
    }
}