package com.pregnancydiet.app.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class AiCredentialResolverTest {
    @Test
    fun `resolver returns free hourly credential in default mode`() = runBlocking {
        val repository = InMemoryAiAccessModeRepository()
        val resolver = AiCredentialResolver(repository, bundledPublicKey = "pk_public_safe", isDebug = false)

        assertTrue(resolver.resolveCredential() is AiCredentialResolution.FreeHourlyCredential)
    }

    @Test
    fun `missing bundled public key returns setup required resolution`() = runBlocking {
        val repository = InMemoryAiAccessModeRepository()
        val resolver = AiCredentialResolver(repository, bundledPublicKey = "", isDebug = false)

        assertTrue(resolver.resolveCredential() is AiCredentialResolution.MissingFreeHourlyKey)
    }

    @Test
    fun `bundled sk key is rejected in release resolver`() = runBlocking {
        val repository = InMemoryAiAccessModeRepository()
        val resolver = AiCredentialResolver(repository, bundledPublicKey = "sk_server_secret", isDebug = false)

        assertTrue(resolver.resolveCredential() is AiCredentialResolution.UnsafeCredential)
    }

    @Test(expected = IllegalStateException::class)
    fun `bundled sk key fails fast in debug resolver`() {
        AiCredentialResolver(InMemoryAiAccessModeRepository(), bundledPublicKey = "sk_server_secret", isDebug = true)
    }

    @Test
    fun `resolver returns user credential when connected`() = runBlocking {
        val repository = InMemoryAiAccessModeRepository()
        repository.saveUserAccountCredential("pk_user_safe")
        val resolver = AiCredentialResolver(repository, bundledPublicKey = "pk_public_safe", isDebug = false)

        assertTrue(resolver.resolveCredential() is AiCredentialResolution.UserAccountCredential)
    }

    @Test
    fun `resolver does not silently fallback from user account to free hourly`() = runBlocking {
        val repository = InMemoryAiAccessModeRepository(initialMode = AiAccessMode.USER_ACCOUNT)
        val resolver = AiCredentialResolver(repository, bundledPublicKey = "pk_public_safe", isDebug = false)

        assertTrue(resolver.resolveCredential() is AiCredentialResolution.MissingUserCredential)
    }
}
