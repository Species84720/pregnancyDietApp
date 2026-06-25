package com.pregnancydiet.app.data

import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.WeightLog
import com.pregnancydiet.app.onboarding.ValidatedOnboardingInput
import com.pregnancydiet.app.pregnancy.PregnancyProgress

interface PregnancyOnboardingRepository {
    suspend fun saveOnboardingProfile(
        uid: String,
        input: ValidatedOnboardingInput,
        pregnancyProfile: PregnancyProfile,
        progress: PregnancyProgress,
    ): Result<OnboardingSaveResult>
}

data class OnboardingSaveResult(
    val pregnancyProfile: PregnancyProfile,
    val weightLog: WeightLog,
)