package com.pregnancydiet.app.data

import com.pregnancydiet.app.model.AuthenticatedUser
import com.pregnancydiet.app.model.UserProfile

interface UserProfileRepository {
    suspend fun createOrUpdateUser(user: AuthenticatedUser): Result<UserProfile>

    suspend fun getUserProfile(uid: String): Result<UserProfile?>
}