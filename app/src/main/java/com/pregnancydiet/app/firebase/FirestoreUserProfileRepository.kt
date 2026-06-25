package com.pregnancydiet.app.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.pregnancydiet.app.data.UserProfileRepository
import com.pregnancydiet.app.model.AuthenticatedUser
import com.pregnancydiet.app.model.UserProfile
import kotlinx.coroutines.tasks.await

class FirestoreUserProfileRepository : UserProfileRepository {
    override suspend fun createOrUpdateUser(user: AuthenticatedUser): Result<UserProfile> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val userRef = firestore.collection(USERS_COLLECTION).document(user.uid)
        val snapshot = userRef.get().await()
        val values = mutableMapOf<String, Any?>(
            "uid" to user.uid,
            "email" to user.email,
            "displayName" to user.displayName,
            "photoUrl" to user.photoUrl,
            "lastLoginAt" to FieldValue.serverTimestamp(),
        )

        if (!snapshot.exists()) {
            values["createdAt"] = FieldValue.serverTimestamp()
            values["onboardingCompleted"] = false
            values["activePregnancyProfileId"] = null
        }

        userRef.set(values, SetOptions.merge()).await()
        userRef.get().await().toUserProfile(user)
    }

    override suspend fun getUserProfile(uid: String): Result<UserProfile?> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val snapshot = firestore.collection(USERS_COLLECTION).document(uid).get().await()
        if (snapshot.exists()) snapshot.toUserProfile() else null
    }
}

private const val USERS_COLLECTION = "users"

private fun DocumentSnapshot.toUserProfile(fallbackUser: AuthenticatedUser? = null): UserProfile = UserProfile(
    uid = getString("uid") ?: id,
    email = getString("email") ?: fallbackUser?.email,
    displayName = getString("displayName") ?: fallbackUser?.displayName,
    photoUrl = getString("photoUrl") ?: fallbackUser?.photoUrl,
    onboardingCompleted = getBoolean("onboardingCompleted") ?: false,
    activePregnancyProfileId = getString("activePregnancyProfileId"),
)