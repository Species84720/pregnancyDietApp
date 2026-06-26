package com.pregnancydiet.app.ai

import android.content.Context

object AiDependencyProvider {
    @Volatile private var accessModeRepository: AiAccessModeRepository = InMemoryAiAccessModeRepository()
    @Volatile private var usageRepository: AiUsageRepository = InMemoryAiUsageRepository()
    @Volatile private var credentialResolver: AiCredentialResolver = AiCredentialResolver(accessModeRepository)
    @Volatile private var provider: AiProvider = PollinationsFrontendAiProvider(credentialResolver, usageRepository)

    fun initialize(context: Context) {
        val localAccess = LocalAiAccessModeRepository(context.applicationContext)
        val localUsage = LocalAiUsageRepository(context.applicationContext, localAccess)
        val resolver = AiCredentialResolver(localAccess)
        accessModeRepository = localAccess
        usageRepository = localUsage
        credentialResolver = resolver
        provider = PollinationsFrontendAiProvider(resolver, localUsage)
    }

    fun aiProvider(): AiProvider = provider
    fun aiAccessModeRepository(): AiAccessModeRepository = accessModeRepository
    fun aiUsageRepository(): AiUsageRepository = usageRepository
    fun aiCredentialResolver(): AiCredentialResolver = credentialResolver
}