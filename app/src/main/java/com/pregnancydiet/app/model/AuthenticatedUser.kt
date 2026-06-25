package com.pregnancydiet.app.model

data class AuthenticatedUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
)