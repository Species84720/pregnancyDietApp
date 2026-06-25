package com.pregnancydiet.app.data

import com.pregnancydiet.app.home.HomeDashboard

interface HomeDashboardRepository {
    suspend fun loadHomeDashboard(uid: String): Result<HomeDashboard?>
}